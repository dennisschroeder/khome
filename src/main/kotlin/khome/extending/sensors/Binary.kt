package khome.extending.sensors

import khome.KhomeApplication
import khome.core.Attributes
import khome.core.State
import khome.entities.EntityId
import khome.entities.devices.Sensor
import khome.extending.Sensor

@Suppress("FunctionName")
inline fun <reified S : State<*>,reified A : Attributes> KhomeApplication.BinarySensor(objectId: String): Sensor<S, A> =
    Sensor(EntityId("binary_sensor", objectId))
