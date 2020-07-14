package khome.extending.entities.actuators.inputs

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.ServiceCommandResolver
import khome.communicating.ServiceType
import khome.entities.Attributes
import khome.entities.EntityId
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import java.time.Instant

typealias InputNumber = Actuator<InputNumberState, InputNumberAttributes>

@Suppress("FunctionName")
fun KhomeApplication.InputNumber(objectId: String): InputNumber =
    Actuator(EntityId("input_number", objectId), ServiceCommandResolver { desiredState ->
        DefaultResolvedServiceCommand(
            service = ServiceType.SET_VALUE,
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
    override val userId: String?,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

