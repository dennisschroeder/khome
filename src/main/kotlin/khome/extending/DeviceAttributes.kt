package khome.extending

import com.google.gson.annotations.SerializedName
import khome.core.Attributes
import java.time.Instant

data class DefaultAttributes(
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class InputTextAttributes(
    val editable: Boolean,
    val min: Int,
    val max: Int,
    val pattern: String,
    val mode: String,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class InputNumberAttributes(
    val initial: Float,
    val editable: Boolean,
    val min: Float,
    val max: Float,
    val step: Float,
    val mode: String,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class InputBooleanAttributes(
    val editable: Boolean,
    val icon: String,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class InputSelectAttributes(
    val options: List<String>,
    val editable: Boolean,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class InputDateAttributes(
    val editable: Boolean,
    val hasDate: Boolean,
    val hasTime: Boolean,
    val year: Int,
    val month: Int,
    val day: Int,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class InputTimeAttributes(
    val editable: Boolean,
    val hasDate: Boolean,
    val hasTime: Boolean,
    val timestamp: Int,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class InputDateTimeAttributes(
    val editable: Boolean,
    val hasDate: Boolean,
    val hasTime: Boolean,
    val year: Int,
    val month: Int,
    val day: Int,
    val timestamp: Int,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class LightAttributes(
    val supported_features: Int,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class DimmableLightAttributes(
    val powerConsumption: Double,
    val supported_features: Int,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

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

enum class Working {
    @SerializedName("Yes") YES,
    @SerializedName("No") NO
}

data class PositionableCoverAttributes(
    val working: Working,
    override val lastChanged: Instant,
    override val lastUpdated: Instant,
    override val friendlyName: String
) : Attributes
