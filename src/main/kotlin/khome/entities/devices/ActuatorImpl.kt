package khome.entities.devices

import com.google.gson.JsonObject
import io.ktor.util.KtorExperimentalAPI
import khome.KhomeApplicationImpl
import khome.communicating.CommandDataWithEntityId
import khome.communicating.ServiceCommandImpl
import khome.communicating.ServiceCommandResolver
import khome.core.mapping.ObjectMapper
import khome.entities.Attributes
import khome.entities.State
import khome.observability.ObservableHistoryNoInitialDelegate
import khome.observability.StateAndAttributes
import khome.observability.Switchable
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
    private val observers: MutableList<SwitchableObserver<S, A, StateAndAttributes<S, A>>> = mutableListOf()
    override lateinit var attributes: A
    override var actualState: S by ObservableHistoryNoInitialDelegate(observers) { attributes }

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
    override fun callService(service: Enum<*>, parameterBag: CommandDataWithEntityId) {
        ServiceCommandImpl(
            service = service.name,
            serviceData = parameterBag
        ).also { app.enqueueStateChange(this, it) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun attachObserver(observer: Switchable) {
        observers.add(observer as SwitchableObserver<S, A, StateAndAttributes<S, A>>)
    }
}
