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
import java.time.LocalDateTime

typealias InputDateTime = Actuator<InputDateTimeState, InputDateTimeAttributes>

@Suppress("FunctionName")
fun KhomeApplication.InputDateTime(objectId: String): InputDateTime =
    Actuator(EntityId("input_datetime", objectId), ServiceCommandResolver { desiredState ->
        DefaultResolvedServiceCommand(
            service = ServiceType.SET_DATETIME,
            serviceData = InputDateTimeServiceData(
                desiredState.value
            )
        )
    })

data class InputDateTimeState(override val value: LocalDateTime) : State<LocalDateTime>

data class InputDateTimeAttributes(
    val editable: Boolean,
    val hasDate: Boolean,
    val hasTime: Boolean,
    val year: Int,
    val month: Int,
    val day: Int,
    val timestamp: Int,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class InputDateTimeServiceData(private val datetime: LocalDateTime) : DesiredServiceData()
