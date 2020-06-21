package khome

import io.ktor.util.KtorExperimentalAPI
import khome.communicating.CommandDataWithEntityId
import khome.communicating.HassApi
import khome.communicating.ServiceCommandImpl
import khome.communicating.ServiceCommandResolver
import khome.communicating.SubscribeEventCommand
import khome.core.ResultResponse
import khome.core.boot.EventResponseConsumer
import khome.core.boot.HassApiInitializer
import khome.core.boot.StateChangeEventSubscriber
import khome.core.boot.authentication.Authenticator
import khome.core.boot.servicestore.ServiceStoreInitializer
import khome.core.boot.statehandling.EntityStateInitializer
import khome.core.koin.KhomeComponent
import khome.core.mapping.ObjectMapper
import khome.entities.ActuatorStateUpdater
import khome.entities.Attributes
import khome.entities.EntityId
import khome.entities.EntityRegistrationValidation
import khome.entities.SensorStateUpdater
import khome.entities.State
import khome.entities.devices.Actuator
import khome.entities.devices.ActuatorImpl
import khome.entities.devices.Sensor
import khome.entities.devices.SensorImpl
import khome.errorHandling.AsyncEventHandlerExceptionHandler
import khome.errorHandling.AsyncObserverExceptionHandler
import khome.errorHandling.ErrorResponseData
import khome.errorHandling.ErrorResponseHandlerImpl
import khome.errorHandling.EventHandlerExceptionHandler
import khome.errorHandling.ObserverExceptionHandler
import khome.events.AsyncEventHandlerImpl
import khome.events.EventHandlerImpl
import khome.events.EventSubscription
import khome.events.SwitchableEventHandler
import khome.observability.AsyncObserverImpl
import khome.observability.HistorySnapshot
import khome.observability.ObserverImpl
import khome.observability.StateAndAttributes
import khome.observability.SwitchableObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.koin.core.get
import org.koin.core.inject
import kotlin.reflect.KClass

typealias StateAndAttributesHistorySnapshot<S, A> = HistorySnapshot<S, A, StateAndAttributes<S, A>>

internal typealias SensorsByApiName = MutableMap<EntityId, SensorImpl<*, *>>
internal typealias ActuatorsByApiName = MutableMap<EntityId, ActuatorImpl<*, *>>
internal typealias ActuatorsByEntity = MutableMap<ActuatorImpl<*, *>, EntityId>
internal typealias EventHandlerByEventType = MutableMap<String, EventSubscription>
internal typealias ErrorResponseHandler = MutableList<SwitchableEventHandler<*>>

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
    private val errorResponseSubscriptions: ErrorResponseHandler = mutableListOf()

    private var observerExceptionHandlerFunction: (Throwable) -> Unit = { exception ->
        logger.error(exception) { "Caught exception in observer" }
    }

    private var eventHandlerExceptionHandlerFunction: (Throwable) -> Unit = { exception ->
        logger.error(exception) { "Caught exception in event handler" }
    }

    init {
        val defaultErrorResponseHandler = ErrorResponseHandler { errorResponseData ->
            logger.error { "CommandId: ${errorResponseData.commandId} -  errorCode: ${errorResponseData.errorResponse.code} ${errorResponseData.errorResponse.message}" }
        }

        attachErrorResponseHandler(defaultErrorResponseHandler)
    }

    override fun <S : State<*>, A : Attributes> Sensor(
        id: EntityId,
        stateType: KClass<*>,
        attributesType: KClass<*>
    ): Sensor<S, A> =
        SensorImpl<S, A>(mapper, stateType, attributesType).also { registerSensor(id, it) }

    override fun <S : State<*>, A : Attributes> Actuator(
        id: EntityId,
        stateType: KClass<*>,
        attributesType: KClass<*>,
        serviceCommandResolver: ServiceCommandResolver<S>
    ): Actuator<S, A> =
        ActuatorImpl<S, A>(
            this,
            mapper,
            serviceCommandResolver,
            stateType,
            attributesType
        ).also { registerActuator(id, it) }

    override fun <S, A> Observer(f: (snapshot: StateAndAttributesHistorySnapshot<S, A>, SwitchableObserver<S, A>) -> Unit): SwitchableObserver<S, A> =
        ObserverImpl(f, ObserverExceptionHandler(observerExceptionHandlerFunction))

    override fun <S, A> AsyncObserver(f: suspend CoroutineScope.(snapshot: StateAndAttributesHistorySnapshot<S, A>, SwitchableObserver<S, A>) -> Unit): SwitchableObserver<S, A> =
        AsyncObserverImpl(f, AsyncObserverExceptionHandler(observerExceptionHandlerFunction))

    override fun overwriteObserverExceptionHandler(f: (Throwable) -> Unit) {
        observerExceptionHandlerFunction = f
    }

    override fun <ED> attachEventHandler(eventType: String, eventHandler: SwitchableEventHandler<ED>, eventDataType: KClass<*>) {
        eventSubscriptionsByEventType[eventType]?.attachEventHandler(eventHandler)
            ?: registerEventSubscription(eventType, eventDataType).attachEventHandler(eventHandler)
    }

    override fun <ED> EventHandler(f: (ED, SwitchableEventHandler<ED>) -> Unit): SwitchableEventHandler<ED> =
        EventHandlerImpl(f, EventHandlerExceptionHandler(eventHandlerExceptionHandlerFunction))

    override fun <ED> AsyncEventHandler(f: suspend CoroutineScope.(ED, SwitchableEventHandler<ED>) -> Unit): SwitchableEventHandler<ED> =
        AsyncEventHandlerImpl(f, AsyncEventHandlerExceptionHandler(eventHandlerExceptionHandlerFunction))

    override fun overwriteEventHandlerExceptionHandler(f: (Throwable) -> Unit) {
        eventHandlerExceptionHandlerFunction = f
    }

    override fun emitEvent(eventType: String, eventData: Any?) {
        hassApi.emitEvent(eventType, eventData)
    }

    override fun ErrorResponseHandler(f: (ErrorResponseData) -> Unit): SwitchableEventHandler<ErrorResponseData> =
        ErrorResponseHandlerImpl(f)

    override fun attachErrorResponseHandler(errorResponseHandler: SwitchableEventHandler<ErrorResponseData>) {
        errorResponseSubscriptions.add(errorResponseHandler)
    }

    override fun <PB> callService(domain: String, service: String, parameterBag: PB) {
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

    override fun runBlocking() =
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
                    eventHandlerByEventType = eventSubscriptionsByEventType,
                    errorResponseHandler = errorResponseSubscriptions
                ).runStartSequenceStep()
            }
        }
}
