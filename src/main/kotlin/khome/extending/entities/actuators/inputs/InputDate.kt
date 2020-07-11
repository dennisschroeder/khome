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
import java.time.LocalDate

typealias InputDate = Actuator<InputDateState, InputDateAttributes>

@Suppress("FunctionName")
fun KhomeApplication.InputDate(objectId: String): InputDate =
    Actuator(EntityId("input_datetime", objectId), ServiceCommandResolver { desiredState ->
        DefaultResolvedServiceCommand(
            service = ServiceType.SET_DATETIME,
            serviceData = InputDateServiceData(desiredState.value)
        )
    })

data class InputDateState(override val value: LocalDate) : State<LocalDate>

data class InputDateAttributes(
    val editable: Boolean,
    val hasDate: Boolean,
    val hasTime: Boolean,
    val year: Int,
    val month: Int,
    val day: Int,
    override val userId: String?,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class InputDateServiceData(private val date: LocalDate) : DesiredServiceData()
