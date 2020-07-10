package khome.extending.entities

import com.google.gson.annotations.SerializedName
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceType
import khome.entities.Attributes
import khome.entities.State
import java.time.Instant

data class SwitchableState(override val value: SwitchableValue) : State<SwitchableValue>

enum class SwitchableValue {
    @SerializedName("on")
    ON,

    @SerializedName("off")
    OFF
}

data class DefaultAttributes(
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

fun mapSwitchable(switchableValue: SwitchableValue) =
    when (switchableValue) {
        SwitchableValue.ON -> DefaultResolvedServiceCommand(
            service = ServiceType.TURN_ON,
            serviceData = EntityIdOnlyServiceData()
        )
        SwitchableValue.OFF -> DefaultResolvedServiceCommand(
            service = ServiceType.TURN_OFF,
            serviceData = EntityIdOnlyServiceData()
        )
    }
