package khome.entities.devices

import com.google.gson.JsonObject
import io.ktor.util.KtorExperimentalAPI
import khome.KhomeApplicationImpl
import khome.communicating.CommandDataWithEntityId
import khome.communicating.ServiceCommandImpl
import khome.communicating.ServiceCommandResolver
import khome.core.mapping.ObjectMapper
import khome.core.observing.CircularBuffer
import khome.entities.Attributes
import khome.entities.State
import khome.observability.ObservableHistoryNoInitialDelegate
import khome.observability.Observer
import khome.observability.StateAndAttributes
import khome.observability.SwitchableObserver
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlin.reflect.KClass

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
internal class ActuatorImpl<S : State<*>, A : Attributes>(
    private val app: KhomeApplicationImpl,
    private val mapper: ObjectMapper,
    private val resolver: ServiceCommandResolver<S>,
    private val stateType: KClass<*>,
    private val attributesType: KClass<*>
) : Actuator<S, A> {
    private val observers: MutableList<Observer<S, A, StateAndAttributes<S, A>>> = mutableListOf()
    override lateinit var attributes: A
    private val _history = CircularBuffer<StateAndAttributes<S, A>>(10)
    override var actualState: S by ObservableHistoryNoInitialDelegate(observers, _history) { attributes }

    @KtorExperimentalAPI
    override var desiredState: S? = null
        set(newDesiredState) {
            newDesiredState?.let { desiredState ->
                val resolvedServiceCommand = resolver.resolve(desiredState)
                ServiceCommandImpl(
                    service = resolvedServiceCommand.service.name,
                    serviceData = resolvedServiceCommand.serviceData
                ).also { app.enqueueStateChange(this, it) }
            }
            field = newDesiredState
        }

    fun trySetActualStateFromAny(newState: JsonObject) {
        @Suppress("UNCHECKED_CAST")
        actualState = mapper.fromJson(newState, stateType.java) as S
    }

    fun trySetAttributesFromAny(newAttributes: JsonObject) {
        @Suppress("UNCHECKED_CAST")
        attributes = mapper.fromJson(newAttributes, attributesType.java) as A
    }

    @KtorExperimentalAPI
    override fun callService(service: String, parameterBag: CommandDataWithEntityId) {
        ServiceCommandImpl(
            service = service,
            serviceData = parameterBag
        ).also { app.enqueueStateChange(this, it) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun attachObserver(observer: SwitchableObserver<S, A>) {
        observers.add(observer as Observer<S, A, StateAndAttributes<S, A>>)
    }

    override val history: List<StateAndAttributes<S, A>>
        get() = _history.snapshot()
}
