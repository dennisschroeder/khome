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
import khome.extending.SettableStateValueServiceData
import java.time.Instant

typealias InputText = Actuator<InputTextState, InputTextAttributes>

@Suppress("FunctionName")
fun KhomeApplication.InputText(objectId: String): InputText =
    Actuator(EntityId("input_text", objectId), ServiceCommandResolver { desiredState ->
        DefaultResolvedServiceCommand(
            service = ServiceType.SET_VALUE,
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
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes
