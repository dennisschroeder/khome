package khome.extending.sensors.binary

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.devices.Sensor
import khome.extending.SwitchableState
import java.time.Instant

typealias MotionSensor = Sensor<SwitchableState, MotionSensorAttributes>

data class MotionSensorAttributes(
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

@Suppress("FunctionName")
fun KhomeApplication.MotionSensor(objectId: String) : MotionSensor = BinarySensor(objectId)
