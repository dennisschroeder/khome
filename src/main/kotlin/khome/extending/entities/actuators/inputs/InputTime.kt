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
import java.time.LocalTime

typealias InputTime = Actuator<InputTimeState, InputTimeAttributes>

@Suppress("FunctionName")
fun KhomeApplication.InputTime(objectId: String): InputTime =
    Actuator(EntityId("input_datetime", objectId), ServiceCommandResolver { desiredState ->
        DefaultResolvedServiceCommand(
            service = ServiceType.SET_DATETIME,
            serviceData = InputTimeServiceData(desiredState.value)
        )
    })

data class InputTimeState(override val value: LocalTime) : State<LocalTime>

data class InputTimeAttributes(
    val editable: Boolean,
    val hasDate: Boolean,
    val hasTime: Boolean,
    val timestamp: Int,
    override val userId: String?,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class InputTimeServiceData(private val time: LocalTime) : DesiredServiceData()
