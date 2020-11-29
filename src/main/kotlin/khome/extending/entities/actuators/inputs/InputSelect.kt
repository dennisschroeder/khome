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
import khome.values.ObjectId
import khome.values.UserId
import khome.values.domain
import khome.values.service
import java.time.Instant

typealias InputSelect<reified S> = Actuator<S, InputSelectAttributes>

@Suppress("FunctionName")
inline fun <reified S : State<Enum<*>>> KhomeApplication.InputSelect(objectId: ObjectId): InputSelect<S> =
    Actuator(EntityId.fromPair("input_select".domain to objectId), ServiceCommandResolver { desiredState ->
        DefaultResolvedServiceCommand(
            service = "select_option".service,
            serviceData = InputSelectServiceData(
                desiredState.value.name
            )
        )
    })

data class InputSelectAttributes(
    val options: List<String>,
    val editable: Boolean,
    override val userId: UserId?,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class InputSelectServiceData(val option: String) : DesiredServiceData()
