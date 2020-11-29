package khome.extending.entities.actuators.inputs

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import khome.values.EntityId
import khome.values.ObjectId
import khome.values.UserId
import khome.values.domain
import khome.values.service
import java.time.Instant

typealias InputNumber = Actuator<InputNumberState, InputNumberAttributes>

@Suppress("FunctionName")
fun KhomeApplication.InputNumber(objectId: ObjectId): InputNumber =
    Actuator(EntityId.fromPair("input_number".domain to objectId), ServiceCommandResolver { desiredState ->
        DefaultResolvedServiceCommand(
            service = "set_value".service,
            serviceData = SettableStateValueServiceData(
                desiredState.value
            )
        )
    })

data class InputNumberState(override val value: Float) : State<Float>

data class InputNumberAttributes(
    val initial: Float,
    val editable: Boolean,
    val min: Float,
    val max: Float,
    val step: Float,
    val mode: String,
    override val userId: UserId?,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes
