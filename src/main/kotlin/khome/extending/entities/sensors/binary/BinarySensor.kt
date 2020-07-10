package khome.extending.entities.sensors.binary

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.EntityId
import khome.entities.State
import khome.entities.devices.Sensor

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> KhomeApplication.BinarySensor(objectId: String): Sensor<S, A> =
    Sensor(EntityId("binary_sensor", objectId), S::class, A::class)
