package khome.extending.entities.actuators.inputs

import khome.KhomeApplication
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.EntityId
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import khome.extending.entities.SwitchableState
import khome.extending.entities.mapSwitchable
import java.time.Instant

typealias InputBoolean = Actuator<SwitchableState, InputBooleanAttributes>

@Suppress("FunctionName")
fun KhomeApplication.InputBoolean(objectId: String): InputBoolean =
    Actuator(EntityId("input_boolean", objectId), ServiceCommandResolver { desiredState ->
        mapSwitchable(desiredState.value)
    })

data class InputBooleanAttributes(
    val editable: Boolean,
    val icon: String,
    override val userId: String?,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes
