package khome.extending.entities.actuators.inputs

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import khome.values.EntityId
import khome.values.FriendlyName
import khome.values.Initial
import khome.values.Max
import khome.values.Min
import khome.values.Mode
import khome.values.ObjectId
import khome.values.Step
import khome.values.UserId
import khome.values.domain
import khome.values.service
import java.time.Instant

typealias InputNumber = Actuator<InputNumberState, InputNumberAttributes>

@Suppress("FunctionName")
fun KhomeApplication.InputNumber(objectId: ObjectId): InputNumber =
    Actuator(
        EntityId.fromPair("input_number".domain to objectId),
        ServiceCommandResolver { desiredState ->
            DefaultResolvedServiceCommand(
                service = "set_value".service,
                serviceData = SettableStateValueServiceData(
                    desiredState.value
                )
            )
        }
    )

data class InputNumberState(override val value: Double) : State<Double>

data class InputNumberAttributes(
    val initial: Initial,
    val editable: Boolean,
    val min: Min,
    val max: Max,
    val step: Step,
    val mode: Mode,
    override val userId: UserId?,
    override val friendlyName: FriendlyName,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes
