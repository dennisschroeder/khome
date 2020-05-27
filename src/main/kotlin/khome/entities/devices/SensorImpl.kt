package khome.entities.devices

import com.google.gson.JsonElement
import khome.core.State
import khome.core.mapping.ObjectMapper
import khome.observability.ObservableHistory
import khome.observability.ObservableHistoryNoInitial
import java.time.OffsetDateTime
import kotlin.reflect.KClass
import kotlin.reflect.cast

interface Sensor<S, SA> {
    val measurement: ObservableHistory<State<S, SA>>
}

internal class SensorImpl<S, SA>(
    private val mapper: ObjectMapper,
    private val stateType: KClass<*>,
    private val attributesValueType: KClass<*>
) : Sensor<S, SA> {
    override var measurement = ObservableHistoryNoInitial<State<S, SA>>()

    @ExperimentalStdlibApi
    fun trySetMeasurementFromAny(
        newValue: Any,
        attributes: JsonElement,
        lastUpdated: OffsetDateTime,
        lastChanged: OffsetDateTime
    ) {
        fun mapToEnumOrNull(value: String) =
            try { mapper.fromJson(value, stateType.java) } catch (e: Exception) { null }

        @Suppress("UNCHECKED_CAST")
        (this as SensorImpl<Any, Any>).measurement.state = State(
            lastChanged = lastChanged.toInstant(),
            value = mapToEnumOrNull(newValue as String) ?: stateType.cast(newValue),
            attributes = mapper.fromJson(attributes, attributesValueType.java),
            lastUpdated = lastUpdated.toInstant()
        )
    }
}
