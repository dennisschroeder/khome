package khome.entities.devices

import com.google.gson.JsonObject
import io.ktor.util.KtorExperimentalAPI
import khome.KhomeApplicationImpl
import khome.communicating.CommandDataWithEntityId
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandImpl
import khome.communicating.ServiceCommandResolver
import khome.core.Attributes
import khome.core.State
import khome.core.mapping.ObjectMapper
import khome.observability.Observable
import khome.observability.ObservableHistoryNoInitialDelegate
import khome.observability.StateWithAttributes
import khome.observability.Switchable
import khome.observability.SwitchableObserver
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging
import kotlin.reflect.KClass

interface Actuator<S : State<*>, SA : Attributes> : Observable<S> {
    val actualState: S
    var attributes: SA
    var desiredState: S?
    fun callService(service: Enum<*>, parameterBag: CommandDataWithEntityId = EntityIdOnlyServiceData())
}

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
internal class ActuatorImpl<S : State<*>, SA : Attributes>(
    private val app: KhomeApplicationImpl,
    private val mapper: ObjectMapper,
    private val resolver: ServiceCommandResolver<S>,
    private val stateType: KClass<*>,
    private val attributesType: KClass<*>
) : Actuator<S, SA> {
    private val logger = KotlinLogging.logger { }
    private val observers: MutableList<SwitchableObserver<S, SA, StateWithAttributes<S, SA>>> = mutableListOf()
    override lateinit var attributes: SA
    override var actualState: S by ObservableHistoryNoInitialDelegate(observers) { attributes }

    @KtorExperimentalAPI
    override var desiredState: S? = null
        set(newDesiredState) {
            newDesiredState?.let { desiredState ->
                val resolvedServiceCommand = resolver.resolve(desiredState)
                ServiceCommandImpl(
                    service = resolvedServiceCommand.service,
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
        attributes = mapper.fromJson(newAttributes, attributesType.java) as SA
    }

    @KtorExperimentalAPI
    override fun callService(service: Enum<*>, parameterBag: CommandDataWithEntityId) {
        ServiceCommandImpl(
            service = service,
            serviceData = parameterBag
        ).also { app.enqueueStateChange(this, it) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun attachObserver(observer: Switchable) {
        observers.add(observer as SwitchableObserver<S, SA, StateWithAttributes<S, SA>>)
    }
}
