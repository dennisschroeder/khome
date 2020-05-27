package khome.entities.devices

import com.google.gson.JsonElement
import io.ktor.util.KtorExperimentalAPI
import khome.KhomeApplicationImpl
import khome.communicating.CommandDataWithEntityId
import khome.communicating.DesiredState
import khome.communicating.DesiredStateImpl
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandImpl
import khome.communicating.ServiceTypeIdentifier
import khome.communicating.ServiceTypeResolver
import khome.core.State
import khome.core.mapping.ObjectMapper
import khome.observability.Observable
import khome.observability.ObservableHistory
import khome.observability.ObservableHistoryNoInitial
import khome.observability.Observer
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.time.OffsetDateTime
import kotlin.reflect.KClass
import kotlin.reflect.cast

interface Actuator<S, SA> : Observable<State<S, SA>> {
    val actualState: ObservableHistory<State<S, SA>>
    var desiredState: DesiredState<S>?
    fun createDesiredState(
        desiredValue: S,
        desiredAttributes: CommandDataWithEntityId = EntityIdOnlyServiceData()
    ): DesiredState<S>

    fun callService(service: ServiceTypeIdentifier, parameterBag: CommandDataWithEntityId = EntityIdOnlyServiceData())
}

@ObsoleteCoroutinesApi
internal class ActuatorImpl<S, SA>(
    private val app: KhomeApplicationImpl,
    private val mapper: ObjectMapper,
    private val resolver: ServiceTypeResolver<S>,
    private val stateType: KClass<*>,
    private val attributesType: KClass<*>
) : Actuator<S, SA> {
    override var actualState = ObservableHistoryNoInitial<State<S, SA>>()

    @KtorExperimentalAPI
    override var desiredState: DesiredState<S>? = null
        set(newDesiredState) {
            newDesiredState?.let {
                ServiceCommandImpl(
                    service = resolver(newDesiredState),
                    serviceData = newDesiredState.attributes
                ).also { app.enqueueStateChange(this, it) }
            }
            field = newDesiredState
        }

    override fun createDesiredState(
        desiredValue: S,
        desiredAttributes: CommandDataWithEntityId
    ): DesiredState<S> = DesiredStateImpl(value = desiredValue, attributes = desiredAttributes)

    @KtorExperimentalAPI
    @ExperimentalStdlibApi
    fun trySetActualStateFromAny(
        lastChanged: OffsetDateTime,
        newValue: Any,
        attributes: JsonElement,
        lastUpdated: OffsetDateTime
    ) {
        fun mapToEnumOrNull(value: String) =
            try {
                mapper.fromJson(value, stateType.java)
            } catch (e: Exception) {
                null
            }

        @Suppress("UNCHECKED_CAST")
        (this as ActuatorImpl<Any, Any>).actualState.state = State(
            lastChanged = lastChanged.toInstant(),
            value = mapToEnumOrNull(newValue as String) ?: stateType.cast(newValue),
            attributes = mapper.fromJson(attributes, attributesType.java),
            lastUpdated = lastUpdated.toInstant()
        )
    }

    override fun attachObserver(observer: Observer<State<S, SA>>) {
        actualState.attachObserver(observer)
    }

    @KtorExperimentalAPI
    override fun callService(service: ServiceTypeIdentifier, parameterBag: CommandDataWithEntityId) {
        ServiceCommandImpl(
            service = service,
            serviceData = parameterBag
        ).also { app.enqueueStateChange(this, it) }
    }
}
