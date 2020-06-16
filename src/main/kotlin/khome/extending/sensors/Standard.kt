package khome.extending.sensors

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.EntityId
import khome.entities.State
import khome.entities.devices.Sensor
import khome.extending.Sensor

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> KhomeApplication.BinarySensor(objectId: String): Sensor<S, A> =
    Sensor(EntityId("binary_sensor", objectId))

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> KhomeApplication.Sensor(objectId: String): Sensor<S, A> =
    Sensor(EntityId("sensor", objectId))
