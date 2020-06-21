package khome

import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.EntityId
import khome.entities.State
import khome.entities.devices.Actuator
import khome.entities.devices.Sensor
import khome.errorHandling.ErrorResponseData
import khome.events.SwitchableEventHandler
import khome.observability.SwitchableObserver
import kotlinx.coroutines.CoroutineScope
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
     * Observer factory function
     *
     * Creates a fresh instance of a observer.
     *
     * @param S the type of the state that represents all state values of the entity.
     * @param A the type of the attributes that represents all attribute values of the entity.
     *
     * @param f the action that gets executed at any state change of the entity it gets attached to.
     *
     * @return [SwitchableObserver]
     */
    @Suppress("FunctionName")
    fun <S, A> Observer(f: (snapshot: StateAndAttributesHistorySnapshot<S, A>, SwitchableObserver<S, A>) -> Unit): SwitchableObserver<S, A>

    /**
     * AsyncObserver factory function
     *
     * Creates a fresh instance of an asynchronous observer.
     *
     * @param S the type of the state that represents all state values of the entity.
     * @param A the type of the attributes that represents all attribute values of the entity.
     * @param f the action that gets executed asynchronously at any state change of the entity it gets attached to.
     *
     * @return [SwitchableObserver]
     */
    @Suppress("FunctionName")
    fun <S, A> AsyncObserver(f: suspend CoroutineScope.(snapshot: StateAndAttributesHistorySnapshot<S, A>, SwitchableObserver<S, A>) -> Unit): SwitchableObserver<S, A>

    /**
     * Overwrites the default observer exception handler action.
     *
     * @param f the action that gets executed when the observer action executes with an exception.
     */
    fun overwriteObserverExceptionHandler(f: (Throwable) -> Unit)

    /**
     * EventHandler factory function
     *
     * Creates a fresh instance of a event handler.
     *
     * @param ED the type of the event data.
     * @param f the action that gets executed any time the home assistant event gets emitted.
     *
     * @return [SwitchableObserver]
     */
    @Suppress("FunctionName")
    fun <ED> EventHandler(f: (ED, SwitchableEventHandler<ED>) -> Unit): SwitchableEventHandler<ED>

    /**
     * AsyncEventHandler factory function.
     *
     * Creates a fresh instance of an asynchronous event handler.
     *
     * @param ED the type of the event data.
     * @param f the action that gets executed asynchronously any time the home assistant event gets emitted.
     *
     * @return [SwitchableObserver]
     */
    @Suppress("FunctionName")
    fun <ED> AsyncEventHandler(f: suspend CoroutineScope.(ED, SwitchableEventHandler<ED>) -> Unit): SwitchableEventHandler<ED>

    /**
     * Attaches an [EventHandler] or [AsyncEventHandler] to Khome and starts the home assistant event subscription.
     */
    fun <ED> attachEventHandler(eventType: String, eventHandler: SwitchableEventHandler<ED>, eventDataType: KClass<*>)

    /**
     * Overwrites the default event handler exception handler action.
     *
     * @param f the action that gets executed when the event handler action executes with an exception.
     */
    fun overwriteEventHandlerExceptionHandler(f: (Throwable) -> Unit)

    /**
     * Emits a home assistant event with optional event data.
     *
     * @param eventType the type of event to emit.
     * @param eventData the data to be send with the event (optional).
     */
    fun emitEvent(eventType: String, eventData: Any? = null)

    /**
     * Error response handler factory function.
     *
     * Creates a fresh instance of an error response handler.
     * @param f the action that gets executed when home assistant responds with an error.
     *
     * @return [SwitchableObserver]
     */
    @Suppress("FunctionName")
    fun ErrorResponseHandler(f: (ErrorResponseData) -> Unit): SwitchableEventHandler<ErrorResponseData>

    /**
     * Attaches an [ErrorResponseHandler] to Khome.
     *
     * @param errorResponseHandler the handler to be attached.
     */
    fun attachErrorResponseHandler(errorResponseHandler: SwitchableEventHandler<ErrorResponseData>)

    /**
     * Sends a service command to home assistant.
     *
     * @param domain the name of the service domain
     * @param service the name of the service to call
     * @param parameterBag the parameters to be send with the command
     */
    fun <PB> callService(domain: String, service: String, parameterBag: PB)
}
