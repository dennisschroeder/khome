package khome.extending.entities.sensors.binary

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.devices.Sensor
import khome.extending.entities.SwitchableState
import java.time.Instant

typealias DayTime = Sensor<SwitchableState, DayTimeAttributes>

@Suppress("FunctionName")
fun KhomeApplication.DayTime(objectId: String): DayTime = BinarySensor(objectId)

data class DayTimeAttributes(
    val after: Instant,
    val before: Instant,
    val nextUpdate: Instant,
    override val userId: String?,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes
