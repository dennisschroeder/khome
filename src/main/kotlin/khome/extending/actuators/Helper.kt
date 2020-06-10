@file:Suppress("FunctionName")

package khome.extending.actuators

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.ServiceCommandResolver
import khome.communicating.ServiceType
import khome.core.State
import khome.entities.EntityId
import khome.extending.Actuator
import khome.extending.InputBooleanAttributes
import khome.extending.InputDateAttributes
import khome.extending.InputDateServiceData
import khome.extending.InputDateState
import khome.extending.InputDateTimeAttributes
import khome.extending.InputDateTimeServiceData
import khome.extending.InputDateTimeState
import khome.extending.InputNumberAttributes
import khome.extending.InputNumberState
import khome.extending.InputSelectAttributes
import khome.extending.InputTextAttributes
import khome.extending.InputTextState
import khome.extending.InputTimeAttributes
import khome.extending.InputTimeServiceData
import khome.extending.InputTimeState
import khome.extending.SettableStateValueServiceData
import khome.extending.SwitchableState
import khome.extending.mapSwitchable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun KhomeApplication.InputText(objectId: String) =
    Actuator<InputTextState, InputTextAttributes>(EntityId("input_text", objectId),
        ServiceCommandResolver { desiredState ->
            DefaultResolvedServiceCommand(
                service = ServiceType.SET_VALUE,
                serviceData = SettableStateValueServiceData(desiredState.value)
            )
        })

fun KhomeApplication.InputNumber(objectId: String) =
    Actuator<InputNumberState, InputNumberAttributes>(
        EntityId("input_number", objectId),
        ServiceCommandResolver { desiredState ->
            DefaultResolvedServiceCommand(
                service = ServiceType.SET_VALUE,
                serviceData = SettableStateValueServiceData(desiredState.value)
            )
        })

fun KhomeApplication.InputBoolean(objectId: String) =
    Actuator<SwitchableState, InputBooleanAttributes>(
        EntityId("input_boolean", objectId),
        ServiceCommandResolver { desiredState ->
            mapSwitchable(desiredState.value)
        }
    )

data class InputSelectServiceData(val option: String) : DesiredServiceData()

inline fun <reified S : State<Enum<*>>> KhomeApplication.InputSelect(objectId: String) =
    Actuator<S, InputSelectAttributes>(
        EntityId("input_select", objectId),
        ServiceCommandResolver { desiredState ->
            DefaultResolvedServiceCommand(
                service = ServiceType.SELECT_OPTION,
                serviceData = InputSelectServiceData(desiredState.value.name)
            )
        })

fun KhomeApplication.InputDate(objectId: String) =
    Actuator<InputDateState, InputDateAttributes>(EntityId("input_datetime", objectId),
        ServiceCommandResolver{ desiredState ->
            DefaultResolvedServiceCommand(
                service = ServiceType.SET_DATETIME,
                serviceData = InputDateServiceData(desiredState.value)
            )
        })

fun KhomeApplication.InputTime(objectId: String) =
    Actuator<InputTimeState, InputTimeAttributes>(EntityId("input_datetime", objectId),
        ServiceCommandResolver { desiredState ->
            DefaultResolvedServiceCommand(
                service = ServiceType.SET_DATETIME,
                serviceData = InputTimeServiceData(desiredState.value)
            )
        })

fun KhomeApplication.InputDateTime(objectId: String) =
    Actuator<InputDateTimeState, InputDateTimeAttributes>(
        EntityId("input_datetime", objectId),
        ServiceCommandResolver { desiredState ->
            DefaultResolvedServiceCommand(
                service = ServiceType.SET_DATETIME,
                serviceData = InputDateTimeServiceData(desiredState.value)
            )
        })
