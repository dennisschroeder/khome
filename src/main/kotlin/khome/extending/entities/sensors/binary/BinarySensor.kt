package khome.extending.entities.sensors.binary

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Sensor
import khome.values.EntityId
import khome.values.ObjectId
import khome.values.domain

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> KhomeApplication.BinarySensor(objectId: ObjectId): Sensor<S, A> =
    Sensor(EntityId.fromPair("binary_sensor".domain to objectId), S::class, A::class)
