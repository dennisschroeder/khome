package khome

import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.entities.devices.Sensor
import khome.errorHandling.ErrorResponseData
import khome.events.EventHandlerFunction
import khome.observability.Switchable
import khome.testing.KhomeTestApplication
import khome.values.Domain
import khome.values.EntityId
import khome.values.EventType
import khome.values.Service
import kotlin.reflect.KClass

/**
 * The Khome Application
 *
 * @author Dennis Schröder
 */
interface KhomeApplication {
    /**
     * Start a single Khome application.
     * This method blocks the current thread.
     */
    fun runBlocking()

    fun runTesting(block: KhomeTestApplication.() -> Unit)

    /**
     * [Sensor] factory function
     *
     * Creates a fresh instance of a sensor entity.
     *
     * @param S the type of the state that represents all state values of the entity. Has to implement the [State] interface.
     * @param A the type of the attributes that represents all attribute values of the entity. Has to implement the [Attributes] interface.
     * @param id the corresponding [EntityId] for the sensor.
     * @param stateType the type param [S] as [KClass].
     * @param attributesType the type param [A] as [KClass].
     *
     * @return [Sensor]
     */
    @Suppress("FunctionName")
    fun <S : State<*>, A : Attributes> Sensor(
        id: EntityId,
        stateType: KClass<*>,
        attributesType: KClass<*>
    ): Sensor<S, A>

    /**
     * [Actuator] factory function
     *
     * Creates a fresh instance of a actuator entity.
     *
     * @param S the type of the state that represents all state values of the entity. Has to implement the [State] interface.
     * @param A the type of the attributes that represents all attribute values of the entity. Has to implement the [Attributes] interface.
     * @param id the corresponding [EntityId] for the sensor.
     * @param stateType the type param [S] as [KClass].
     * @param attributesType the type param [A] as [KClass].
     * @param serviceCommandResolver the serviceCommandResolver instance. @See [ServiceCommandResolver] for more.
     *
     * @return [Actuator]
     */
    @Suppress("FunctionName")
    fun <S : State<*>, A : Attributes> Actuator(
        id: EntityId,
        stateType: KClass<*>,
        attributesType: KClass<*>,
        serviceCommandResolver: ServiceCommandResolver<S>
    ): Actuator<S, A>

    /**
     * Overwrites the default observer exception handler action.
     *
     * @param f the action that gets executed when the observer action executes with an exception.
     */
    fun setObserverExceptionHandler(f: (Throwable) -> Unit)

    /**
     * Attaches an [EventHandlerFunction] to Khome and starts the home assistant event subscription.
     */
    fun <ED> attachEventHandler(
        eventType: EventType,
        eventDataType: KClass<*>,
        eventHandler: EventHandlerFunction<ED>
    ): Switchable

    /**
     * Overwrites the default event handler exception handler action.
     *
     * @param f the action that gets executed when the event handler action executes with an exception.
     */
    fun setEventHandlerExceptionHandler(f: (Throwable) -> Unit)

    /**
     * Emits a home assistant event with optional event data.
     *
     * @param eventType the type of event to emit.
     * @param eventData the data to be send with the event (optional).
     */
    fun emitEvent(eventType: String, eventData: Any? = null)

    /**
     * Attaches an error response handler to Khome.
     *
     * @param errorResponseHandler the handler to be attached.
     */
    fun setErrorResponseHandler(errorResponseHandler: (ErrorResponseData) -> Unit)

    /**
     * Sends a service command to home assistant.
     *
     * @param domain the name of the service domain
     * @param service the name of the service to call
     * @param parameterBag the parameters to be send with the command
     */
    fun <PB> callService(domain: Domain, service: Service, parameterBag: PB)

    /**
     * Gets executed when the application did all checks and set all initial states
     *
     * @param f the callback function that gets executed on application ready state
     */
    fun onApplicationReady(f: KhomeApplication.() -> Unit)
}
