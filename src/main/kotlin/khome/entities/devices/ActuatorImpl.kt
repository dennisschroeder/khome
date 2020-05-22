package khome.entities.devices

import khome.KhomeApplicationImpl
import khome.core.State
import khome.observability.Observable
import khome.observability.ObservableHistory
import khome.observability.ObservableHistoryNoInitial
import khome.observability.Observer
import khome.communicating.DesiredState
import khome.communicating.DesiredStateImpl
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.time.OffsetDateTime
import kotlin.reflect.KClass
import kotlin.reflect.cast

interface Actuator<S> : Observable<State<S>> {
    val actualState: ObservableHistory<State<S>>
    var desiredState: DesiredState<S>?
    fun createDesiredState(desiredValue: S?, desiredAttributes: Map<String, Any>?): DesiredState<S>
}

@ObsoleteCoroutinesApi
internal class ActuatorImpl<S>(private val app: KhomeApplicationImpl, private val type: KClass<*>) : Actuator<S> {
    override var actualState = ObservableHistoryNoInitial<State<S>>()
    override var desiredState: DesiredState<S>? = null
        set(newDesiredState) {
            newDesiredState?.let { app.enqueueStateChange(this, newDesiredState) }
            field = newDesiredState
        }

    override fun createDesiredState(
        desiredValue: S?,
        desiredAttributes: Map<String, Any>?
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
