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
import java.time.LocalDateTime

typealias InputDateTime = Actuator<InputDateTimeState, InputDateTimeAttributes>

@Suppress("FunctionName")
fun KhomeApplication.InputDateTime(objectId: ObjectId): InputDateTime =
    Actuator(
        EntityId.fromPair("input_datetime".domain to objectId),
        ServiceCommandResolver { desiredState ->
            DefaultResolvedServiceCommand(
                service = "set_datetime".service,
                serviceData = InputDateTimeServiceData(
                    desiredState.value
                )
            )
        }
    )

data class InputDateTimeState(override val value: LocalDateTime) : State<LocalDateTime>

data class InputDateTimeAttributes(
    val editable: Boolean,
    val hasDate: Boolean,
    val hasTime: Boolean,
    val year: Int,
    val month: Int,
    val day: Int,
    val timestamp: Int,
    override val userId: UserId?,
    override val friendlyName: FriendlyName,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class InputDateTimeServiceData(private val datetime: LocalDateTime) : DesiredServiceData()
