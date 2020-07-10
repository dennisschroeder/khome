package khome.extending.entities.actuators.inputs

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.ServiceCommandResolver
import khome.communicating.ServiceType
import khome.entities.Attributes
import khome.entities.EntityId
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import java.time.Instant

typealias InputSelect<reified S> = Actuator<S, InputSelectAttributes>

@Suppress("FunctionName")
inline fun <reified S : State<Enum<*>>> KhomeApplication.InputSelect(objectId: String): InputSelect<S> =
    Actuator(EntityId("input_select", objectId), ServiceCommandResolver { desiredState ->
        DefaultResolvedServiceCommand(
            service = ServiceType.SELECT_OPTION,
            serviceData = InputSelectServiceData(
                desiredState.value.name
            )
        )
    })

data class InputSelectAttributes(
    val options: List<String>,
    val editable: Boolean,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class InputSelectServiceData(val option: String) : DesiredServiceData()
