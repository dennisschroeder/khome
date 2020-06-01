package khome.entities.devices

import com.google.gson.JsonElement
import io.ktor.util.KtorExperimentalAPI
import khome.core.State
import khome.core.mapping.ObjectMapper
import khome.observability.Observable
import khome.observability.ObservableHistory
import khome.observability.ObservableHistoryNoInitial
import khome.observability.Switchable
import mu.KotlinLogging
import java.time.OffsetDateTime
import kotlin.reflect.KClass
import kotlin.reflect.cast

interface Sensor<S, SA> : Observable<State<S, SA>> {
    val measurement: ObservableHistory<State<S, SA>>
}

internal class SensorImpl<S, SA>(
    private val mapper: ObjectMapper,
    private val stateType: KClass<*>,
    private val attributesValueType: KClass<*>
) : Sensor<S, SA> {
    private val logger = KotlinLogging.logger {  }
    override var measurement = ObservableHistoryNoInitial<State<S, SA>>()

    override fun attachObserver(observer: Switchable) {
        measurement.attachObserver(observer)
    }

    @KtorExperimentalAPI
    @ExperimentalStdlibApi
    fun trySetMeasurementFromAny(
        newValue: Any,
        attributes: JsonElement,
        lastUpdated: OffsetDateTime,
        lastChanged: OffsetDateTime
    ) {
        fun mapToEnumOrNull(value: String) =
            try {
                mapper.fromJson("\"$value\"", stateType.java)
            } catch (e: Exception) {
                logger.warn(e) { "$value could not be mapped to ${stateType.simpleName}"}
                null
            }

        @Suppress("UNCHECKED_CAST")
        (this as SensorImpl<Any, Any>).measurement.state = State(
            lastChanged = lastChanged.toInstant(),
            value = mapToEnumOrNull(newValue as String) ?: stateType.cast(newValue),
            attributes = mapper.fromJson(attributes, attributesValueType.java),
            lastUpdated = lastUpdated.toInstant()
        )
    }
}
