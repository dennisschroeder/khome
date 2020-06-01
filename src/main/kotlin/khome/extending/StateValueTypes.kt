package khome.extending

import com.google.gson.annotations.SerializedName

enum class SwitchableValue {
    @SerializedName("on") ON,
    @SerializedName("off") OFF
}

enum class SunValue {
    @SerializedName("above_horizon")
    ABOVE_HORIZON,
    @SerializedName("below_horizon")
    BELOW_HORIZON
}
