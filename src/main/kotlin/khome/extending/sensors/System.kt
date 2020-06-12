package khome.extending.sensors

import khome.KhomeApplication
import khome.entities.EntityId
import khome.entities.devices.Sensor
import khome.extending.Sensor
import khome.extending.SunAttributes
import khome.extending.SunState

@Suppress("FunctionName")
fun KhomeApplication.Sun(): Sensor<SunState, SunAttributes> =
    Sensor(EntityId("sun", "sun"))
