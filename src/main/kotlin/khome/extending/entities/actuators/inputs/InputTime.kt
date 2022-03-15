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
import java.time.LocalTime

typealias InputTime = Actuator<InputTimeState, InputTimeAttributes>

@Suppress("FunctionName")
fun KhomeApplication.InputTime(objectId: ObjectId): InputTime =
    Actuator(
        EntityId.fromPair("input_datetime".domain to objectId),
        ServiceCommandResolver { desiredState ->
            DefaultResolvedServiceCommand(
                service = "set_datetime".service,
                serviceData = InputTimeServiceData(desiredState.value)
            )
        }
    )

data class InputTimeState(override val value: LocalTime) : State<LocalTime>

data class InputTimeAttributes(
    val editable: Boolean,
    val hasDate: Boolean,
    val hasTime: Boolean,
    val timestamp: Int,
    override val userId: UserId?,
    override val friendlyName: FriendlyName,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class InputTimeServiceData(private val time: LocalTime) : DesiredServiceData()
