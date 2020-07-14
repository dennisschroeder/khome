package khome.extending.entities.sensors.binary

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.devices.Sensor
import khome.extending.entities.SwitchableState
import khome.extending.entities.SwitchableValue
import khome.extending.entities.sensors.changedFrom
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

val MotionSensor.motionDetected
    get() = changedFrom(SwitchableValue.OFF to SwitchableValue.ON)

inline fun MotionSensor.onMotion(crossinline f: MotionSensor.() -> Unit) =
    attachObserver {
        if (motionDetected) f(this)
    }
