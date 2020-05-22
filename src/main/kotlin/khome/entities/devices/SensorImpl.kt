package khome.entities.devices

import khome.core.State
import khome.observability.ObservableHistory
import khome.observability.ObservableHistoryNoInitial
import mu.KotlinLogging
import java.time.OffsetDateTime
import kotlin.reflect.KClass
import kotlin.reflect.cast

interface Sensor<S> {
    val measurement: ObservableHistory<State<S>>
}

internal class SensorImpl<S>(private val type: KClass<*>) : Sensor<S> {
    val logger = KotlinLogging.logger { }
    override var measurement = ObservableHistoryNoInitial<State<S>>()

    @ExperimentalStdlibApi
    fun trySetMeasurementFromAny(
        newValue: Any,
        attributes: Map<String, Any>,
        lastUpdated: OffsetDateTime,
        lastChanged: OffsetDateTime
    ) {
        @Suppress("UNCHECKED_CAST")
        (this as SensorImpl<Any>).measurement.state = State(
            lastChanged = lastChanged.toInstant(),
            value = type.cast(newValue),
            attributes = attributes,
            lastUpdated = lastUpdated.toInstant()
        )
    }
}
