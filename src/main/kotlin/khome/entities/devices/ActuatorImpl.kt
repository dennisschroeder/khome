package khome.entities.devices

import com.google.gson.JsonObject
import io.ktor.util.KtorExperimentalAPI
import khome.KhomeApplicationImpl
import khome.communicating.CommandDataWithEntityId
import khome.communicating.ServiceCommandImpl
import khome.communicating.ServiceCommandResolver
import khome.core.mapping.ObjectMapperInterface
import khome.core.observing.CircularBuffer
import khome.entities.Attributes
import khome.entities.State
import khome.errorHandling.ObserverExceptionHandler
import khome.observability.ObservableDelegateNoInitial
import khome.observability.Observer
import khome.observability.ObserverFunction
import khome.observability.ObserverImpl
import khome.observability.StateAndAttributes
import khome.observability.Switchable
import khome.values.Service
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlin.reflect.KClass

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
internal class ActuatorImpl<S : State<*>, A : Attributes>(
    private val app: KhomeApplicationImpl,
    private val mapper: ObjectMapperInterface,
    private val resolver: ServiceCommandResolver<S>,
    private val stateType: KClass<*>,
    private val attributesType: KClass<*>
) : Actuator<S, A> {
    private val observers: MutableList<Observer<Actuator<S, A>>> = mutableListOf()
    override lateinit var attributes: A
    private val _history = CircularBuffer<StateAndAttributes<S, A>>(10)
    override var actualState: S by ObservableDelegateNoInitial(this, observers, _history)

    override val history: List<StateAndAttributes<S, A>>
        get() = _history.snapshot

    override val observerCount: Int
        get() = observers.size

    @KtorExperimentalAPI
    override var desiredState: S? = null
        set(newDesiredState) {
            newDesiredState?.let { desiredState ->
                val resolvedServiceCommand = resolver.resolve(desiredState)
                ServiceCommandImpl(
                    domain = resolvedServiceCommand.domain,
                    service = resolvedServiceCommand.service,
                    serviceData = resolvedServiceCommand.serviceData
                ).also { app.enqueueStateChange(this, it) }
            }
            field = newDesiredState
        }

    fun trySetActualStateFromAny(newState: JsonObject) {
        @Suppress("UNCHECKED_CAST")
        actualState = mapper.fromJson(newState, stateType.java) as S
        checkNotNull(actualState.value) { "State value shall not be null. Please check your State definition  " }
    }

    fun trySetAttributesFromAny(newAttributes: JsonObject) {
        @Suppress("UNCHECKED_CAST")
        attributes = mapper.fromJson(newAttributes, attributesType.java) as A
    }

    @KtorExperimentalAPI
    override fun callService(service: Service, parameterBag: CommandDataWithEntityId) {
        ServiceCommandImpl(
            service = service,
            serviceData = parameterBag
        ).also { app.enqueueStateChange(this, it) }
    }

    override fun attachObserver(observer: ObserverFunction<Actuator<S, A>>): Switchable =
        ObserverImpl(
            observer,
            ObserverExceptionHandler(app.observerExceptionHandlerFunction)
        ).also { observers.add(it) }
}
