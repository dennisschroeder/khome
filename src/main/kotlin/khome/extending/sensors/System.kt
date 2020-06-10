package khome.extending.sensors

import khome.KhomeApplication
import khome.core.Attributes
import khome.core.State
import khome.entities.EntityId
import khome.entities.devices.Sensor
import khome.extending.Sensor
import khome.extending.SunValue
import java.time.Instant

data class SunState(override val value: SunValue) : State<SunValue>
data class SunAttributes(
    val next_dawn: Instant,
    val next_dusk: Instant,
    val next_midnight: Instant,
    val next_noon: Instant,
    val next_rising: Instant,
    val next_setting: Instant,
    val elevation: Double,
    val azimuth: Double,
    val rising: Boolean,
    override val lastChanged: Instant,
    override val lastUpdated: Instant,
    override val friendlyName: String
) : Attributes

@Suppress("FunctionName")
fun KhomeApplication.Sun(): Sensor<SunState, SunAttributes> =
    Sensor(EntityId("sun", "sun"))
