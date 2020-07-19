package khome.extending.entities.sensors

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Sensor
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineScope
import java.time.Instant

typealias LuminanceSensor = Sensor<LuminanceState, LuminanceAttributes>

@Suppress("FunctionName")
fun KhomeApplication.LuminanceSensor(objectId: String): LuminanceSensor = Sensor(objectId)

data class LuminanceState(override val value: Double) : State<Double>

data class LuminanceAttributes(
    val unitOfMeasurement: String,
    override val userId: String?,
    override val lastChanged: Instant,
    override val lastUpdated: Instant,
    override val friendlyName: String
) : Attributes

fun LuminanceSensor.isBrighterThan(value: Double) = measurement.value > value
fun LuminanceSensor.isDarkerThan(value: Double) = measurement.value < value

inline fun LuminanceSensor.onIncreasing(
    threshold: Double? = null,
    crossinline f: LuminanceSensor.(Switchable) -> Unit
) =
    attachObserver { observer ->
        threshold?.let {
            if (history[1].state.value < measurement.value &&
                measurement.value > threshold
            ) f(this, observer)
        } ?: run {
            if (history[1].state.value < measurement.value)
                f(this, observer)
        }
    }

inline fun LuminanceSensor.onIncreasingAsync(
    threshold: Double? = null,
    crossinline f: suspend LuminanceSensor.(Switchable, CoroutineScope) -> Unit
) =
    attachAsyncObserver { observer, coroutineScope ->
        threshold?.let {
            if (history[1].state.value < measurement.value &&
                measurement.value > threshold
            ) f(this, observer, coroutineScope)
        } ?: run {
            if (history[1].state.value < measurement.value)
                f(this, observer, coroutineScope)
        }
    }

inline fun LuminanceSensor.onDecreasing(
    threshold: Double? = null,
    crossinline f: LuminanceSensor.(Switchable) -> Unit
) =
    attachObserver { observer ->
        threshold?.let {
            if (history[1].state.value > measurement.value &&
                measurement.value < threshold
            ) f(this, observer)
        } ?: run {
            if (history[1].state.value > measurement.value)
                f(this, observer)
        }
    }

inline fun LuminanceSensor.onDecreasingAsync(
    threshold: Double? = null,
    crossinline f: suspend LuminanceSensor.(Switchable, CoroutineScope) -> Unit
) =
    attachAsyncObserver { observer, coroutineScope ->
        threshold?.let {
            if (history[1].state.value > measurement.value &&
                measurement.value < threshold
            ) f(this, observer, coroutineScope)
        } ?: run {
            if (history[1].state.value > measurement.value)
                f(this, observer, coroutineScope)
        }
    }
