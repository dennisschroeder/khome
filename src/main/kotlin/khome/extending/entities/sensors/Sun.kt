package khome.extending.entities.sensors

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.EntityId
import khome.entities.State
import khome.entities.devices.Sensor
import khome.extending.entities.Sensor
import java.time.Instant

typealias Sun = Sensor<SunState, SunAttributes>

@Suppress("FunctionName")
fun KhomeApplication.Sun(): Sun =
    Sensor(EntityId("sun", "sun"))

data class SunState(override val value: SunValue) : State<SunValue>

enum class SunValue {
    @SerializedName("above_horizon")
    ABOVE_HORIZON,

    @SerializedName("below_horizon")
    BELOW_HORIZON
}

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
    override val userId: String?,
    override val lastChanged: Instant,
    override val lastUpdated: Instant,
    override val friendlyName: String
) : Attributes

val Sun.hasRisen
    get() = changedFrom(SunValue.BELOW_HORIZON to SunValue.ABOVE_HORIZON)

val Sun.hasSet
    get() = changedFrom(SunValue.BELOW_HORIZON to SunValue.ABOVE_HORIZON)

val Sun.isAboveHorizon
    get() = measurement.value == SunValue.ABOVE_HORIZON

val Sun.isBelowHorizon
    get() = measurement.value == SunValue.BELOW_HORIZON
