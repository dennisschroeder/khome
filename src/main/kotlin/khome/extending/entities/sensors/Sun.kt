package khome.extending.entities.sensors

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Sensor
import khome.extending.entities.Sensor
import khome.observability.Switchable
import khome.values.Azimuth
import khome.values.Elevation
import khome.values.EntityId
import khome.values.FriendlyName
import khome.values.Rising
import khome.values.UserId
import khome.values.domain
import khome.values.objectId
import java.time.Instant

typealias Sun = Sensor<SunState, SunAttributes>

@Suppress("FunctionName")
fun KhomeApplication.Sun(): Sun =
    Sensor(EntityId.fromPair("sun".domain to "sun".objectId))

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
    val elevation: Elevation,
    val azimuth: Azimuth,
    val rising: Rising,
    override val userId: UserId?,
    override val lastChanged: Instant,
    override val lastUpdated: Instant,
    override val friendlyName: FriendlyName
) : Attributes

val Sun.isAboveHorizon
    get() = measurement.value == SunValue.ABOVE_HORIZON

val Sun.isBelowHorizon
    get() = measurement.value == SunValue.BELOW_HORIZON

val Sun.isRising
    get() = attributes.rising == Rising.TRUE

fun Sun.onSunrise(f: Sun.(Switchable) -> Unit) =
    onMeasurementValueChangedFrom(SunValue.BELOW_HORIZON to SunValue.ABOVE_HORIZON, f)

fun Sun.onSunset(f: Sun.(Switchable) -> Unit) =
    onMeasurementValueChangedFrom(SunValue.ABOVE_HORIZON to SunValue.BELOW_HORIZON, f)
