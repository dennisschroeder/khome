package khome

import io.ktor.util.KtorExperimentalAPI
import khome.communicating.CommandDataWithEntityId
import khome.communicating.HassApi
import khome.communicating.ServiceCommandImpl
import khome.communicating.ServiceTypeIdentifier
import khome.communicating.ServiceTypeResolver
import khome.communicating.SubscribeEventCommand
import khome.core.ResultResponse
import khome.core.State
import khome.core.boot.EventResponseConsumer
import khome.core.boot.HassApiInitializer
import khome.core.boot.StateChangeEventSubscriber
import khome.core.boot.authentication.Authenticator
import khome.core.boot.servicestore.ServiceStoreInitializer
import khome.core.boot.statehandling.EntityStateInitializer
import khome.core.koin.KhomeComponent
import khome.core.mapping.ObjectMapper
import khome.entities.ActuatorStateUpdater
import khome.entities.EntityId
import khome.entities.SensorStateUpdater
import khome.entities.devices.Actuator
import khome.entities.devices.ActuatorImpl
import khome.entities.devices.Sensor
import khome.entities.devices.SensorImpl
import khome.events.AsyncEventHandler
import khome.events.EventHandler
import khome.events.EventHandlerImpl
import khome.events.EventSubscription
import khome.helper.SWITCHABLE_RESOLVER
import khome.observability.AsyncObserver
import khome.observability.Observer
import khome.observability.ObserverImpl
import khome.observability.WithHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging
import org.koin.core.get
import org.koin.core.inject
import kotlin.reflect.KClass

internal typealias SensorsByApiName = MutableMap<EntityId, SensorImpl<*, *>>
internal typealias ActuatorsByApiName = MutableMap<EntityId, ActuatorImpl<*, *>>
internal typealias ActuatorsByEntity = MutableMap<ActuatorImpl<*, *>, EntityId>
internal typealias ServiceTypeResolverByDomain = MutableMap<String, ServiceTypeResolver<*>>
internal typealias EventHandlerByEventType = MutableMap<String, EventSubscription>

interface KhomeApplication {
    suspend fun run()
    fun <S, SA> createSensor(id: EntityId, stateValueType: KClass<*>, attributesValueType: KClass<*>): Sensor<S, SA>
    fun <S, SA> createActuator(id: EntityId, stateValueType: KClass<*>, attributesValueType: KClass<*>): Actuator<S, SA>
    fun <S, SA> createObserver(f: (WithHistory<State<S, SA>>) -> Unit): Observer<State<S, SA>>
    fun <S, SA> createAsyncObserver(f: suspend CoroutineScope.(snapshot: WithHistory<State<S, SA>>) -> Unit): Observer<State<S, SA>>
    fun <S> registerServiceTypeResolver(domain: String, resolver: ServiceTypeResolver<S>)
    fun <ED> attachEventHandler(eventType: String, eventHandler: EventHandler<ED>, eventDataType: KClass<*>)
    fun <ED> createEventHandler(f: (ED) -> Unit): EventHandler<ED>
    fun <ED> createAsyncEventHandler(f: suspend CoroutineScope.(ED) -> Unit): EventHandler<ED>
    fun <PB> callService(domain: String, service: ServiceTypeIdentifier, parameterBag: PB)
}

@OptIn(
    ExperimentalStdlibApi::class,
    KtorExperimentalAPI::class,
    ObsoleteCoroutinesApi::class,
    ExperimentalCoroutinesApi::class
)
internal class KhomeApplicationImpl : KhomeApplication {
    private val logger = KotlinLogging.logger { }
    private val koin = object : KhomeComponent {}
    private val hassClient: HassClient by koin.inject()
    private val hassApi: HassApi by koin.inject()
    private val mapper: ObjectMapper by koin.inject()

    private val sensorsByApiName: SensorsByApiName = mutableMapOf()
    private val actuatorsByApiName: ActuatorsByApiName = mutableMapOf()
    private val actuatorsByEntity: ActuatorsByEntity = mutableMapOf()

    private val serviceTypeResolverByDomain: ServiceTypeResolverByDomain = mutableMapOf()
    private val eventSubscriptionsByEventType: EventHandlerByEventType = mutableMapOf()

    init {
        registerServiceTypeResolver("input_boolean", SWITCHABLE_RESOLVER)
        registerServiceTypeResolver("light", SWITCHABLE_RESOLVER)
    }

    override fun <S, SA> createSensor(
        id: EntityId,
        stateValueType: KClass<*>,
        attributesValueType: KClass<*>
    ): Sensor<S, SA> =
        SensorImpl<S, SA>(mapper, stateValueType, attributesValueType).also { registerSensor(id, it) }

    override fun <S, SA> createActuator(
        id: EntityId,
        stateValueType: KClass<*>,
        attributesValueType: KClass<*>
    ): Actuator<S, SA> =
        ActuatorImpl<S, SA>(
            this,
            mapper,
            getServiceTypeResolver(id.domain),
            stateValueType,
            attributesValueType
        ).also { registerActuator(id, it) }

    override fun <T, SA> createObserver(f: (WithHistory<State<T, SA>>) -> Unit): Observer<State<T, SA>> =
        ObserverImpl(f)

    override fun <T, SA> createAsyncObserver(f: suspend CoroutineScope.(snapshot: WithHistory<State<T, SA>>) -> Unit): Observer<State<T, SA>> =
        AsyncObserver(f)

    @Suppress("UNCHECKED_CAST")
    override fun <S> registerServiceTypeResolver(domain: String, resolver: ServiceTypeResolver<S>) {
        serviceTypeResolverByDomain[domain] = resolver as ServiceTypeResolver<*>
    }

    override fun <ED> attachEventHandler(eventType: String, eventHandler: EventHandler<ED>, eventDataType: KClass<*>) {
        eventSubscriptionsByEventType[eventType]?.attachEventHandler(eventHandler)
            ?: registerEventSubscription(eventType, eventDataType).attachEventHandler(eventHandler)
    }

    override fun <ED> createEventHandler(f: (ED) -> Unit): EventHandler<ED> =
        EventHandlerImpl(f)

    override fun <ED> createAsyncEventHandler(f: suspend CoroutineScope.(ED) -> Unit): EventHandler<ED> =
        AsyncEventHandler(f)

    override fun <PB> callService(domain: String, service: ServiceTypeIdentifier, parameterBag: PB) {
        ServiceCommandImpl<PB>(
            domain = domain,
            service = service,
            serviceData = parameterBag
        ).also { hassApi.sendHassApiCommand(it) }
    }

    private fun getServiceTypeResolver(domain: String) = serviceTypeResolverByDomain[domain]
        ?: throw RuntimeException("No service type resolver found for $domain. Please register one.")

    private fun registerSensor(entityId: EntityId, sensor: SensorImpl<*, *>) {
        sensorsByApiName[entityId] = sensor
        logger.info { "Registered sensor with id: $entityId" }
    }

    private fun registerActuator(entityId: EntityId, actuator: ActuatorImpl<*, *>) {
        actuatorsByApiName[entityId] = actuator
        actuatorsByEntity[actuator] = entityId
        logger.info { "Registered actuator with id: $entityId" }
    }

    private fun registerEventSubscription(eventType: String, eventDataType: KClass<*>) =
        EventSubscription(mapper, eventDataType).also { eventSubscriptionsByEventType[eventType] = it }

    internal fun <State, StateAttributes> enqueueStateChange(
        actuator: ActuatorImpl<State, StateAttributes>,
        commandImpl: ServiceCommandImpl<CommandDataWithEntityId>
    ) {
        val entityId = actuatorsByEntity[actuator] ?: throw RuntimeException("Entity not registered: $actuator")
        commandImpl.apply {
            domain = entityId.domain
            serviceData?.entityId = entityId
        }
        hassApi.sendHassApiCommand(commandImpl)
    }

    override suspend fun run() =
        hassClient.startSession {

            Authenticator(
                khomeSession = this,
                configuration = get()
            ).runBootSequence()

            ServiceStoreInitializer(
                khomeSession = this,
                serviceStore = get()
            ).runBootSequence()

            HassApiInitializer(khomeSession = this).runBootSequence()

            eventSubscriptionsByEventType.forEach { entry ->
                SubscribeEventCommand(entry.key).also { command -> hassApi.sendHassApiCommand(command) }
            }

            consumeSingleMessage<ResultResponse>()
                .takeIf { resultResponse -> resultResponse.success }
                ?.let { logger.info { "Subscribed to events" } }

            EntityStateInitializer(
                khomeSession = this,
                sensorStateUpdater = SensorStateUpdater(sensorsByApiName),
                actuatorStateUpdater = ActuatorStateUpdater(actuatorsByApiName)
            ).runBootSequence()

            StateChangeEventSubscriber(khomeSession = this).runBootSequence()

            EventResponseConsumer(
                khomeSession = this,
                objectMapper = get(),
                sensorStateUpdater = SensorStateUpdater(sensorsByApiName),
                actuatorStateUpdater = ActuatorStateUpdater(actuatorsByApiName),
                eventHandlerByEventType = eventSubscriptionsByEventType
            ).runBootSequence()
        }
}
