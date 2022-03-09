package khome.extending.entities.actuators.inputs

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import khome.values.EntityId
import khome.values.FriendlyName
import khome.values.ObjectId
import khome.values.Option
import khome.values.UserId
import khome.values.domain
import khome.values.service
import java.time.Instant

typealias InputSelect = Actuator<InputSelectState, InputSelectAttributes>

@Suppress("FunctionName")
fun KhomeApplication.InputSelect(objectId: ObjectId): InputSelect =
    Actuator(
        EntityId.fromPair("input_select".domain to objectId),
        ServiceCommandResolver { desiredState ->
            DefaultResolvedServiceCommand(
                service = "select_option".service,
                serviceData = InputSelectServiceData(
                    desiredState.value
                )
            )
        }
    )

data class InputSelectAttributes(
    val options: List<Option>,
    val editable: Boolean,
    override val userId: UserId?,
    override val friendlyName: FriendlyName,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class InputSelectServiceData(val option: Option) : DesiredServiceData()

data class InputSelectState(override val value: Option) : State<Option>
