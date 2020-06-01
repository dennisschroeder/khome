package khome.extending

import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCallResolver
import khome.communicating.ServiceType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

internal val SWITCHABLE_VALUE_RESOLVER: ServiceCallResolver<SwitchableValue> = { desiredState ->
    when (desiredState.value) {
        SwitchableValue.ON -> DefaultResolvedServiceCommand(
            service = ServiceType.TURN_ON,
            serviceData = EntityIdOnlyServiceData()
        )
        SwitchableValue.OFF -> DefaultResolvedServiceCommand(
            service = ServiceType.TURN_ON,
            serviceData = EntityIdOnlyServiceData()
        )
    }
}

internal data class SettableStateValueServiceData<S>(private val value: S) : DesiredServiceData()

internal val INPUT_TEXT_RESOLVER: ServiceCallResolver<String> = { desiredState ->
    DefaultResolvedServiceCommand(
        service = ServiceType.SET_VALUE,
        serviceData = SettableStateValueServiceData(desiredState.value)
    )
}

internal val INPUT_NUMBER_RESOLVER: ServiceCallResolver<Float> = { desiredState ->
    DefaultResolvedServiceCommand(
        service = ServiceType.SET_VALUE,
        serviceData = SettableStateValueServiceData(desiredState.value)
    )
}

internal data class InputSelectServiceData(val option: String) : DesiredServiceData()

internal val INPUT_SELECT_RESOLVER: ServiceCallResolver<Enum<*>> = { desiredState ->
    DefaultResolvedServiceCommand(
        service = ServiceType.SELECT_OPTION,
        serviceData = InputSelectServiceData(desiredState.value.name)
    )
}

internal data class InputDateTimeServiceData(
    private val date: LocalDate?,
    private val time: LocalTime?,
    private val datetime: LocalDateTime?
) : DesiredServiceData()

internal val INPUT_DATETIME_RESOLVER: ServiceCallResolver<Any> = { desiredState ->

    when (desiredState.value) {
        is LocalTime -> DefaultResolvedServiceCommand(
            service = ServiceType.SET_DATETIME,
            serviceData = InputDateTimeServiceData(null, desiredState.value as LocalTime, null)
        )
        is LocalDate -> DefaultResolvedServiceCommand(
            service = ServiceType.SET_DATETIME,
            serviceData = InputDateTimeServiceData(desiredState.value as LocalDate, null, null)
        )
        is LocalDateTime -> DefaultResolvedServiceCommand(
            service = ServiceType.SET_DATETIME,
            serviceData = InputDateTimeServiceData(null, null, desiredState.value as LocalDateTime)
        )
        else -> throw IllegalStateException("${desiredState.value::class.simpleName} is not supported by this service call resolver.")
    }
}
