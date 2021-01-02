package khome.extending.entities

import com.google.gson.annotations.SerializedName
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.EntityIdOnlyServiceData
import khome.entities.Attributes
import khome.entities.State
import khome.values.FriendlyName
import khome.values.UserId
import khome.values.service
import java.time.Instant

data class SwitchableState(override val value: SwitchableValue) : State<SwitchableValue>

enum class SwitchableValue {
    @SerializedName("on")
    ON,

    @SerializedName("off")
    OFF,

    @SerializedName("unavailable")
    UNAVAILABLE
}

data class DefaultAttributes(
    override val userId: UserId?,
    override val friendlyName: FriendlyName,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

fun mapSwitchable(switchableValue: SwitchableValue) =
    when (switchableValue) {
        SwitchableValue.ON -> DefaultResolvedServiceCommand(
            service = "turn_on".service,
            serviceData = EntityIdOnlyServiceData()
        )
        SwitchableValue.OFF -> DefaultResolvedServiceCommand(
            service = "turn_off".service,
            serviceData = EntityIdOnlyServiceData()
        )

        SwitchableValue.UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
    }
