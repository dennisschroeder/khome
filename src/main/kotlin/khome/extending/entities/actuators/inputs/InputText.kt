package khome.extending.entities.actuators.inputs

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.values.EntityId
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import khome.values.ObjectId
import khome.values.UserId
import khome.values.domain
import khome.values.service
import java.time.Instant

typealias InputText = Actuator<InputTextState, InputTextAttributes>

@Suppress("FunctionName")
fun KhomeApplication.InputText(objectId: ObjectId): InputText =
    Actuator(EntityId.fromPair("input_text".domain to objectId), ServiceCommandResolver { desiredState ->
        DefaultResolvedServiceCommand(
            service = "set_value".service,
            serviceData = SettableStateValueServiceData(
                desiredState.value
            )
        )
    })

data class InputTextState(override val value: String) : State<String>

data class InputTextAttributes(
    val editable: Boolean,
    val min: Int,
    val max: Int,
    val pattern: String,
    val mode: String,
    override val userId: UserId?,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes
