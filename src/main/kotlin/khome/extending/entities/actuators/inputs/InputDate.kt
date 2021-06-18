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
import khome.values.UserId
import khome.values.domain
import khome.values.service
import java.time.Instant
import java.time.LocalDate

typealias InputDate = Actuator<InputDateState, InputDateAttributes>

@Suppress("FunctionName")
fun KhomeApplication.InputDate(objectId: ObjectId): InputDate =
    Actuator(
        EntityId.fromPair("input_datetime".domain to objectId),
        ServiceCommandResolver { desiredState ->
            DefaultResolvedServiceCommand(
                service = "set_datetime".service,
                serviceData = InputDateServiceData(desiredState.value)
            )
        }
    )

data class InputDateState(override val value: LocalDate) : State<LocalDate>

data class InputDateAttributes(
    val editable: Boolean,
    val hasDate: Boolean,
    val hasTime: Boolean,
    val year: Int,
    val month: Int,
    val day: Int,
    override val userId: UserId?,
    override val friendlyName: FriendlyName,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class InputDateServiceData(private val date: LocalDate) : DesiredServiceData()
