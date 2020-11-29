package khome.extending.entities.sensors.binary

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.devices.Sensor
import khome.extending.entities.SwitchableState
import khome.extending.entities.SwitchableValue
import khome.extending.entities.sensors.measurementValueChangedFrom
import khome.values.ObjectId
import khome.values.UserId
import java.time.Instant

typealias MotionSensor = Sensor<SwitchableState, MotionSensorAttributes>

@Suppress("FunctionName")
fun KhomeApplication.MotionSensor(objectId: ObjectId): MotionSensor = BinarySensor(objectId)

data class MotionSensorAttributes(
    override val userId: UserId?,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

inline fun MotionSensor.onMotionAlarm(
    crossinline f: MotionSensor.() -> Unit
) =
    attachObserver {
        if (measurementValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON))
            f(this)
    }

inline fun MotionSensor.onMotionAlarmCleared(
    crossinline f: MotionSensor.() -> Unit
) =
    attachObserver {
        if (measurementValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF))
            f(this)
    }
