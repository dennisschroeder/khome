package khome.entities.devices

import com.google.gson.JsonElement
import io.ktor.util.KtorExperimentalAPI
import khome.KhomeApplicationImpl
import khome.communicating.CommandDataWithEntityId
import khome.communicating.DesiredState
import khome.communicating.DesiredStateImpl
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCallResolver
import khome.communicating.ServiceCommandImpl
import khome.communicating.ServiceTypeIdentifier
import khome.core.State
import khome.core.mapping.ObjectMapper
import khome.observability.Observable
import khome.observability.ObservableHistory
import khome.observability.ObservableHistoryNoInitial
import khome.observability.Switchable
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging
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
    private val resolver: ServiceCallResolver<S>,
    private val stateType: KClass<*>,
    private val attributesType: KClass<*>
) : Actuator<S, SA> {
    private val logger = KotlinLogging.logger {  }
    override var actualState = ObservableHistoryNoInitial<State<S, SA>>()

    @KtorExperimentalAPI
    override var desiredState: DesiredState<S>? = null
        set(newDesiredState) {
            newDesiredState?.let {
                val resolvedServiceCommand = resolver(newDesiredState)
                ServiceCommandImpl(
                    service = resolvedServiceCommand.service,
                    serviceData = resolvedServiceCommand.serviceData
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
        fun mapToStateOrNull(value: String) =
            try {
                mapper.fromJson("\"$value\"", stateType.java)
            } catch (e: Exception) {
                logger.warn(e) { "$value could not be mapped to ${stateType.simpleName}" }
                null
            }

        @Suppress("UNCHECKED_CAST")
        (this as ActuatorImpl<Any, Any>).actualState.state = State(
            lastChanged = lastChanged.toInstant(),
            value = mapToStateOrNull(newValue as String) ?: stateType.cast(newValue),
            attributes = mapper.fromJson(attributes, attributesType.java),
            lastUpdated = lastUpdated.toInstant()
        )
    }

    override fun attachObserver(observer: Switchable) {
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
