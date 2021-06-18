package khome

import io.ktor.util.KtorExperimentalAPI
import khome.communicating.CommandDataWithEntityId
import khome.communicating.HassApiClient
import khome.communicating.ServiceCommandImpl
import khome.communicating.ServiceCommandResolver
import khome.core.boot.EventResponseConsumer
import khome.core.boot.HassApiInitializer
import khome.core.boot.StateChangeEventSubscriber
import khome.core.boot.authentication.Authenticator
import khome.core.boot.servicestore.ServiceStoreInitializer
import khome.core.boot.statehandling.EntityStateInitializer
import khome.core.boot.subscribing.HassEventSubscriber
import khome.core.koin.KoinContainer
import khome.core.mapping.ObjectMapperInterface
import khome.entities.ActuatorStateUpdater
import khome.entities.Attributes
import khome.entities.EntityRegistrationValidation
import khome.entities.SensorStateUpdater
import khome.entities.State
import khome.entities.devices.Actuator
import khome.entities.devices.ActuatorImpl
import khome.entities.devices.Sensor
import khome.entities.devices.SensorImpl
import khome.errorHandling.ErrorResponseData
import khome.events.EventHandlerFunction
import khome.events.EventSubscription
import khome.observability.Switchable
import khome.testing.KhomeTestApplication
import khome.testing.KhomeTestApplicationImpl
import khome.values.Domain
import khome.values.EntityId
import khome.values.EventType
import khome.values.Service
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlin.collections.set
import kotlin.reflect.KClass

internal typealias SensorsByApiName = MutableMap<EntityId, SensorImpl<*, *>>
internal typealias ActuatorsByApiName = MutableMap<EntityId, ActuatorImpl<*, *>>
internal typealias ActuatorsByEntity = MutableMap<ActuatorImpl<*, *>, EntityId>
internal typealias EventHandlerByEventType = MutableMap<EventType, EventSubscription<*>>
internal typealias HassAPiCommandHistory = MutableMap<EntityId, ServiceCommandImpl<CommandDataWithEntityId>>
internal typealias ApplicationReadyCallbacks = MutableList<KhomeApplication.() -> Unit>

@OptIn(
    ExperimentalStdlibApi::class,
    KtorExperimentalAPI::class,
    ObsoleteCoroutinesApi::class,
    ExperimentalCoroutinesApi::class
)
internal class KhomeApplicationImpl : KhomeApplication {

    private val logger = KotlinLogging.logger { }
    private val hassClient: HassClient by KoinContainer.inject()
    private val hassApi: HassApiClient by KoinContainer.inject()
    private val mapper: ObjectMapperInterface by KoinContainer.inject()

    private val sensorsByApiName: SensorsByApiName = mutableMapOf()
    private val actuatorsByApiName: ActuatorsByApiName = mutableMapOf()
    private val actuatorsByEntity: ActuatorsByEntity = mutableMapOf()
    private val hassAPiCommandHistory: HassAPiCommandHistory = mutableMapOf()

    private val eventSubscriptionsByEventType: EventHandlerByEventType = mutableMapOf()

    private val applicationReadyCallbacks: ApplicationReadyCallbacks = mutableListOf()

    var observerExceptionHandlerFunction: (Throwable) -> Unit = { exception ->
        logger.error(exception) { "Caught exception in observer" }
    }

    var eventHandlerExceptionHandlerFunction: (Throwable) -> Unit = { exception ->
        logger.error(exception) { "Caught exception in event handler" }
    }

    var errorResponseHandlerFunction: (ErrorResponseData) -> Unit = { errorResponseData ->
        logger.error { "CommandId: ${errorResponseData.commandId} - errorCode: ${errorResponseData.errorResponse.code} | message: ${errorResponseData.errorResponse.message}" }
    }

    override fun <S : State<*>, A : Attributes> Sensor(
        id: EntityId,
        stateType: KClass<*>,
        attributesType: KClass<*>
    ): Sensor<S, A> =
        SensorImpl<S, A>(this, mapper, stateType, attributesType).also { registerSensor(id, it) }

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

    override fun setObserverExceptionHandler(f: (Throwable) -> Unit) {
        observerExceptionHandlerFunction = f
    }

    @Suppress("UNCHECKED_CAST")
    override fun <ED> attachEventHandler(
        eventType: EventType,
        eventDataType: KClass<*>,
        eventHandler: EventHandlerFunction<ED>
    ): Switchable =
        eventSubscriptionsByEventType[eventType]?.attachEventHandler(
            eventHandler as EventHandlerFunction<Any?>
        )
            ?: registerEventSubscription<ED>(eventType, eventDataType).attachEventHandler(eventHandler)

    override fun setEventHandlerExceptionHandler(f: (Throwable) -> Unit) {
        eventHandlerExceptionHandlerFunction = f
    }

    override fun emitEvent(eventType: String, eventData: Any?) {
        hassApi.emitEvent(eventType, eventData)
    }

    override fun setErrorResponseHandler(errorResponseHandler: (ErrorResponseData) -> Unit) {
        errorResponseHandlerFunction = errorResponseHandler
    }

    override fun <PB> callService(domain: Domain, service: Service, parameterBag: PB) {
        ServiceCommandImpl(
            domain = domain,
            service = service,
            serviceData = parameterBag
        ).also { hassApi.sendCommand(it) }
    }

    private fun registerSensor(entityId: EntityId, sensor: SensorImpl<*, *>) {
        check(!sensorsByApiName.containsKey(entityId)) { "Sensor with id: $entityId already exists." }
        sensorsByApiName[entityId] = sensor
        logger.info { "Registered Sensor with id: $entityId" }
    }

    private fun registerActuator(entityId: EntityId, actuator: ActuatorImpl<*, *>) {
        check(!actuatorsByApiName.containsKey(entityId)) { "Actuator with id: $entityId already exists." }
        actuatorsByApiName[entityId] = actuator
        actuatorsByEntity[actuator] = entityId
        logger.info { "Registered Actuator with id: $entityId" }
    }

    private fun <ED> registerEventSubscription(eventType: EventType, eventDataType: KClass<*>) =
        EventSubscription<ED>(this, mapper, eventDataType).also { eventSubscriptionsByEventType[eventType] = it }

    internal fun <S : State<*>, SA : Attributes> enqueueStateChange(
        actuator: ActuatorImpl<S, SA>,
        commandImpl: ServiceCommandImpl<CommandDataWithEntityId>
    ) {
        val entityId = actuatorsByEntity[actuator] ?: throw RuntimeException("Entity not registered: $actuator")
        commandImpl.apply {
            if (domain == null) domain = entityId.domain
            serviceData?.entityId = entityId
        }
        hassAPiCommandHistory[entityId] = commandImpl
        hassApi.sendCommand(commandImpl)
    }

    override fun onApplicationReady(f: KhomeApplication.() -> Unit) {
        applicationReadyCallbacks.add(f)
    }

    override fun runBlocking() =
        runBlocking {
            hassClient.startSession {
                val authenticator: Authenticator by KoinContainer.inject { parametersOf(this) }
                val serviceStoreInitializer: ServiceStoreInitializer by KoinContainer.inject { parametersOf(this) }
                val hassApiInitializer: HassApiInitializer by KoinContainer.inject { parametersOf(this) }
                val hassEventSubscriber: HassEventSubscriber by KoinContainer.inject {
                    parametersOf(this, eventSubscriptionsByEventType)
                }

                val entityStateInitializer: EntityStateInitializer by KoinContainer.inject {
                    parametersOf(
                        this,
                        SensorStateUpdater(sensorsByApiName),
                        ActuatorStateUpdater(actuatorsByApiName),
                        EntityRegistrationValidation(actuatorsByApiName, sensorsByApiName)
                    )
                }

                val stateChangeEventSubscriber: StateChangeEventSubscriber by KoinContainer.inject { parametersOf(this) }
                val eventResponseConsumer: EventResponseConsumer by KoinContainer.inject {
                    parametersOf(
                        this,
                        SensorStateUpdater(sensorsByApiName),
                        ActuatorStateUpdater(actuatorsByApiName),
                        eventSubscriptionsByEventType,
                        errorResponseHandlerFunction
                    )
                }

                authenticator.authenticate()
                serviceStoreInitializer.initialize()
                hassApiInitializer.initialize()
                hassEventSubscriber.subscribe()
                entityStateInitializer.initialize()
                stateChangeEventSubscriber.subscribe()
                applicationReadyCallbacks.forEach { it.invoke(this@KhomeApplicationImpl) }
                eventResponseConsumer.consumeBlocking()
            }
        }

    override fun runTesting(block: KhomeTestApplication.() -> Unit) {
        applicationReadyCallbacks.forEach { it.invoke(this@KhomeApplicationImpl) }
        val testApp = KhomeTestApplicationImpl(
            sensorsByApiName,
            actuatorsByApiName,
            actuatorsByEntity,
            mapper,
            hassAPiCommandHistory
        ).apply(block)
        testApp.reset()
    }
}
