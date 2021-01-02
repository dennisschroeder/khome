package khome.extending.entities.sensors.binary

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.devices.Sensor
import khome.extending.entities.SwitchableState
import khome.values.FriendlyName
import khome.values.ObjectId
import khome.values.UserId
import java.time.Instant

typealias DayTime = Sensor<SwitchableState, DayTimeAttributes>

@Suppress("FunctionName")
fun KhomeApplication.DayTime(objectId: ObjectId): DayTime = BinarySensor(objectId)

data class DayTimeAttributes(
    val after: Instant,
    val before: Instant,
    val nextUpdate: Instant,
    override val userId: UserId?,
    override val friendlyName: FriendlyName,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes
