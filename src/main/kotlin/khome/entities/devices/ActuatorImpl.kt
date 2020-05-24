package khome.entities.devices

import io.ktor.util.KtorExperimentalAPI
import khome.KhomeApplicationImpl
import khome.communicating.CommandDataWithEntityId
import khome.communicating.DesiredState
import khome.communicating.DesiredStateImpl
import khome.communicating.HassApiCommandImpl
import khome.communicating.ServiceTypeResolver
import khome.core.State
import khome.observability.Observable
import khome.observability.ObservableHistory
import khome.observability.ObservableHistoryNoInitial
import khome.observability.Observer
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.time.OffsetDateTime
import kotlin.reflect.KClass
import kotlin.reflect.cast

interface Actuator<S> : Observable<State<S>> {
    val actualState: ObservableHistory<State<S>>
    var desiredState: DesiredState<S>?
    fun createDesiredState(desiredValue: S?, desiredAttributes: CommandDataWithEntityId?): DesiredState<S>
}

@ObsoleteCoroutinesApi
internal class ActuatorImpl<S>(private val app: KhomeApplicationImpl, private val resolver: ServiceTypeResolver<S>, private val type: KClass<*>) : Actuator<S> {
    override var actualState = ObservableHistoryNoInitial<State<S>>()
    @KtorExperimentalAPI
    override var desiredState: DesiredState<S>? = null
        set(newDesiredState) {
            newDesiredState?.let {
                HassApiCommandImpl(
                    service = resolver(newDesiredState),
                    serviceData = newDesiredState.attributes
                ).also {
                    app.enqueueStateChange(this, it)
                }
            }
            field = newDesiredState
        }

    override fun createDesiredState(
        desiredValue: S?,
        desiredAttributes: CommandDataWithEntityId?
    ): DesiredState<S> = DesiredStateImpl(value = desiredValue, attributes = desiredAttributes)

    @ExperimentalStdlibApi
    fun trySetActualStateFromAny(
        lastChanged: OffsetDateTime,
        newValue: Any,
        attributes: Map<String, Any>,
        lastUpdated: OffsetDateTime
    ) {
        @Suppress("UNCHECKED_CAST")
        (this as ActuatorImpl<Any>).actualState.state = State(
            lastChanged = lastChanged.toInstant(),
            value = type.cast(newValue),
            attributes = attributes,
            lastUpdated = lastUpdated.toInstant()
        )
    }

    override fun attachObserver(observer: Observer<State<S>>) {
        actualState.attachObserver(observer)
    }
}
