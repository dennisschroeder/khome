package khome.extending.entities.sensors

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.EntityId
import khome.entities.State
import khome.entities.devices.Sensor
import khome.extending.entities.Sensor
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineScope
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

val Sun.isAboveHorizon
    get() = measurement.value == SunValue.ABOVE_HORIZON

val Sun.isBelowHorizon
    get() = measurement.value == SunValue.BELOW_HORIZON

fun Sun.onSunrise(f: Sun.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (measurementValueChangedFrom(SunValue.BELOW_HORIZON to SunValue.ABOVE_HORIZON))
            f(this, observer)
    }

fun Sun.onSunriseAsync(f: suspend Sun.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, coroutineScope ->
        if (measurementValueChangedFrom(SunValue.BELOW_HORIZON to SunValue.ABOVE_HORIZON))
            f(this, observer, coroutineScope)
    }

fun Sun.onSunset(f: Sun.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (measurementValueChangedFrom(SunValue.ABOVE_HORIZON to SunValue.BELOW_HORIZON))
            f(this, observer)
    }

fun Sun.onSunsetAsync(f: suspend Sun.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, coroutineScope ->
        if (measurementValueChangedFrom(SunValue.ABOVE_HORIZON to SunValue.BELOW_HORIZON))
            f(this, observer, coroutineScope)
    }
