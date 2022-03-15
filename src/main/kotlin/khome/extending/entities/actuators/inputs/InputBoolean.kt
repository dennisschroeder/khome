package khome.extending.entities.actuators.inputs

import khome.KhomeApplication
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import khome.extending.entities.SwitchableState
import khome.extending.entities.mapSwitchable
import khome.values.EntityId
import khome.values.FriendlyName
import khome.values.Icon
import khome.values.ObjectId
import khome.values.UserId
import khome.values.domain
import java.time.Instant

typealias InputBoolean = Actuator<SwitchableState, InputBooleanAttributes>

@Suppress("FunctionName")
fun KhomeApplication.InputBoolean(objectId: ObjectId): InputBoolean =
    Actuator(
        EntityId.fromPair("input_boolean".domain to objectId),
        ServiceCommandResolver { desiredState ->
            mapSwitchable(desiredState.value)
        }
    )

data class InputBooleanAttributes(
    val editable: Boolean,
    val icon: Icon,
    override val userId: UserId?,
    override val friendlyName: FriendlyName,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes
