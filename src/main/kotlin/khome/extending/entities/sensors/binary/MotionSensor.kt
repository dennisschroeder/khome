package khome.extending.entities.sensors.binary

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.devices.Sensor
import khome.extending.entities.SwitchableState
import khome.extending.entities.SwitchableValue
import khome.extending.entities.sensors.measurementValueChangedFrom
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineScope
import java.time.Instant

typealias MotionSensor = Sensor<SwitchableState, MotionSensorAttributes>

@Suppress("FunctionName")
fun KhomeApplication.MotionSensor(objectId: String): MotionSensor = BinarySensor(objectId)

data class MotionSensorAttributes(
    override val userId: String?,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

inline fun MotionSensor.onMotionAlarm(crossinline f: MotionSensor.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (measurementValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON))
            f(this, observer)
    }

inline fun MotionSensor.onMotionAlarmAsync(crossinline f: suspend MotionSensor.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, coroutineScope ->
        if (measurementValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON))
            f(this, observer, coroutineScope)
    }

inline fun MotionSensor.onMotionAlarmClearance(crossinline f: MotionSensor.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (measurementValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF))
            f(this, observer)
    }

inline fun MotionSensor.onMotionAlarmClearanceAsync(crossinline f: suspend MotionSensor.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, coroutineScope ->
        if (measurementValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF))
            f(this, observer, coroutineScope)
    }
