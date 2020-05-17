package khome.entities.devices

import khome.core.State
import khome.observability.ObservableHistory
import khome.observability.ObservableHistoryNoInitial
import mu.KotlinLogging
import java.time.OffsetDateTime
import kotlin.reflect.KClass
import kotlin.reflect.cast

interface Actuator<S> {
    val actualState: ObservableHistory<State<S>>
    var desiredState: S?
}

internal class ActuatorImpl<S>(private val type: KClass<*>) : Actuator<S> {
    val logger = KotlinLogging.logger {  }
    override var actualState = ObservableHistoryNoInitial<State<S>>()
    override var desiredState: S? = null
        set(value) {
            value?.let { TODO("Attach to api") }
            field = value
        }

    @ExperimentalStdlibApi
    fun trySetActualStateFromAny(
        lastChanged: OffsetDateTime,
        newValue: Any,
        attributes: Map<String, Any>,
        lastUpdated: OffsetDateTime
    ) {
        @Suppress("UNCHECKED_CAST")
        (this as ActuatorImpl<Any>).actualState.state = State(
            lastChanged = lastChanged,
            value = type.cast(newValue),
            attributes = attributes,
            lastUpdated = lastUpdated
        )
    }
}
