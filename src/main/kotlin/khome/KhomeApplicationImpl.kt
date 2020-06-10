@file:Suppress("FunctionName")

package khome

import io.ktor.client.statement.HttpResponse
import io.ktor.util.KtorExperimentalAPI
import khome.communicating.CommandDataWithEntityId
import khome.communicating.HassApi
import khome.communicating.ServiceCommandImpl
import khome.communicating.ServiceCommandResolver
import khome.communicating.SubscribeEventCommand
import khome.core.Attributes
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
import khome.entities.EntityRegistrationValidation
import khome.entities.SensorStateUpdater
import khome.entities.devices.Actuator
import khome.entities.devices.ActuatorImpl
import khome.entities.devices.Sensor
import khome.entities.devices.SensorImpl
import khome.events.AsyncEventHandler
import khome.events.EventHandlerImpl
import khome.events.EventSubscription
import khome.observability.AsyncObserver
import khome.observability.ObserverImpl
import khome.observability.StateWithAttributes
import khome.observability.Switchable
import khome.observability.WithHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.koin.core.get
import org.koin.core.inject
import kotlin.reflect.KClass

internal typealias SensorsByApiName = MutableMap<EntityId, SensorImpl<*, *>>
internal typealias ActuatorsByApiName = MutableMap<EntityId, ActuatorImpl<*, *>>
internal typealias ActuatorsByEntity = MutableMap<ActuatorImpl<*, *>, EntityId>
internal typealias EventHandlerByEventType = MutableMap<String, EventSubscription>

interface KhomeApplication {
    fun run()
    fun <S : State<*>, SA : Attributes> Sensor(
        id: EntityId,
        stateValueType: KClass<*>,
        attributesValueType: KClass<*>
    ): Sensor<S, SA>

    fun <S : State<*>, SA : Attributes> Actuator(
        id: EntityId,
        stateValueType: KClass<*>,
        attributesValueType: KClass<*>,
        serviceCommandResolver: ServiceCommandResolver<S>
    ): Actuator<S, SA>

    fun <S, SA> createObserver(f: (WithHistory<S, SA, StateWithAttributes<S, SA>>, Switchable) -> Unit): Switchable
    fun <S, SA> createAsyncObserver(f: suspend CoroutineScope.(snapshot: WithHistory<S, SA, StateWithAttributes<S, SA>>, Switchable) -> Unit): Switchable

    fun attachEventHandler(eventType: String, eventHandler: Switchable, eventDataType: KClass<*>)
    fun <ED> createEventHandler(f: (ED, Switchable) -> Unit): Switchable
    fun <ED> createAsyncEventHandler(f: suspend CoroutineScope.(ED, Switchable) -> Unit): Switchable
    fun emitEvent(eventType: String, eventData: Any? = null)
    fun emitEventAsync(eventType: String, eventData: Any? = null): Deferred<HttpResponse>
    fun <PB> callService(domain: String, service: Enum<*>, parameterBag: PB)
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

    private val eventSubscriptionsByEventType: EventHandlerByEventType = mutableMapOf()

    override fun <S : State<*>, SA : Attributes> Sensor(
        id: EntityId,
        stateValueType: KClass<*>,
        attributesValueType: KClass<*>
    ): Sensor<S, SA> =
        SensorImpl<S, SA>(mapper, stateValueType, attributesValueType).also { registerSensor(id, it) }

    override fun <S : State<*>, SA : Attributes> Actuator(
        id: EntityId,
        stateValueType: KClass<*>,
        attributesValueType: KClass<*>,
        serviceCommandResolver: ServiceCommandResolver<S>
    ): Actuator<S, SA> =
        ActuatorImpl<S, SA>(
            this,
            mapper,
            serviceCommandResolver,
            stateValueType,
            attributesValueType
        ).also { registerActuator(id, it) }

    override fun <S, SA> createObserver(f: (WithHistory<S, SA, StateWithAttributes<S, SA>>, Switchable) -> Unit): Switchable =
        ObserverImpl(f)

    override fun <S, SA> createAsyncObserver(f: suspend CoroutineScope.(snapshot: WithHistory<S, SA, StateWithAttributes<S, SA>>, Switchable) -> Unit): Switchable =
        AsyncObserver(f)

    override fun attachEventHandler(
        eventType: String, eventHandler: Switchable, eventDataType: KClass<*>) {
        eventSubscriptionsByEventType[eventType]?.attachEventHandler(eventHandler)
            ?: registerEventSubscription(eventType, eventDataType).attachEventHandler(eventHandler)
    }

    override fun <ED> createEventHandler(f: (ED, Switchable) -> Unit): Switchable =
        EventHandlerImpl(f)

    override fun <ED> createAsyncEventHandler(f: suspend CoroutineScope.(ED, Switchable) -> Unit): Switchable =
        AsyncEventHandler(f)

    override fun emitEvent(eventType: String, eventData: Any?) {
        hassApi.emitEvent(eventType, eventData)
    }

    override fun emitEventAsync(eventType: String, eventData: Any?): Deferred<HttpResponse> =
        hassApi.emitEventAsync(eventType, eventData)

    override fun <PB> callService(domain: String, service: Enum<*>, parameterBag: PB) {
        ServiceCommandImpl<PB>(
            domain = domain,
            service = service,
            serviceData = parameterBag
        ).also { hassApi.sendHassApiCommand(it) }
    }

    private fun registerSensor(entityId: EntityId, sensor: SensorImpl<*, *>) {
        check(!sensorsByApiName.containsKey(entityId)) { "Sensor with id: $entityId already exists." }
        sensorsByApiName[entityId] = sensor
    }

    private fun registerActuator(entityId: EntityId, actuator: ActuatorImpl<*, *>) {
        check(!actuatorsByApiName.containsKey(entityId)) { "Actuator with id: $entityId already exists." }
        actuatorsByApiName[entityId] = actuator
        actuatorsByEntity[actuator] = entityId
    }

    private fun registerEventSubscription(eventType: String, eventDataType: KClass<*>) =
        EventSubscription(mapper, eventDataType).also { eventSubscriptionsByEventType[eventType] = it }

    internal fun <S : State<*>, SA : Attributes> enqueueStateChange(
        actuator: ActuatorImpl<S, SA>,
        commandImpl: ServiceCommandImpl<CommandDataWithEntityId>
    ) {
        val entityId = actuatorsByEntity[actuator] ?: throw RuntimeException("Entity not registered: $actuator")
        commandImpl.apply {
            domain = entityId.domain
            serviceData?.entityId = entityId
        }
        hassApi.sendHassApiCommand(commandImpl)
    }

    override fun run() =
        runBlocking {
            hassClient.startSession {

                Authenticator(
                    khomeSession = this,
                    configuration = get()
                ).runStartSequenceStep()

                ServiceStoreInitializer(
                    khomeSession = this,
                    serviceStore = get()
                ).runStartSequenceStep()

                HassApiInitializer(khomeSession = this).runStartSequenceStep()

                eventSubscriptionsByEventType.forEach { entry ->
                    SubscribeEventCommand(entry.key).also { command -> hassApi.sendHassApiCommand(command) }
                    consumeSingleMessage<ResultResponse>()
                        .takeIf { resultResponse -> resultResponse.success }
                        ?.let { logger.info { "Subscribed to event: ${entry.key}" } }
                }

                EntityStateInitializer(
                    khomeSession = this,
                    sensorStateUpdater = SensorStateUpdater(sensorsByApiName),
                    actuatorStateUpdater = ActuatorStateUpdater(actuatorsByApiName),
                    entityRegistrationValidation = EntityRegistrationValidation(actuatorsByApiName, sensorsByApiName)
                ).runStartSequenceStep()

                StateChangeEventSubscriber(khomeSession = this).runStartSequenceStep()

                EventResponseConsumer(
                    khomeSession = this,
                    objectMapper = get(),
                    sensorStateUpdater = SensorStateUpdater(sensorsByApiName),
                    actuatorStateUpdater = ActuatorStateUpdater(actuatorsByApiName),
                    eventHandlerByEventType = eventSubscriptionsByEventType
                ).runStartSequenceStep()
            }
        }
}
