package khome.extending

import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.ServiceCallResolver
import khome.communicating.ServiceType

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

internal data class SettableInputValueDesiredAttributes<S>(private val value: S) : DesiredServiceData()

internal val INPUT_TEXT_RESOLVER: ServiceCallResolver<String> = { desiredState ->
    DefaultResolvedServiceCommand(service = ServiceType.SET_VALUE, serviceData = SettableInputValueDesiredAttributes(desiredState.value))
}

internal val INPUT_NUMBER_RESOLVER: ServiceCallResolver<Float> = { desiredState ->
    DefaultResolvedServiceCommand(service = ServiceType.SET_VALUE, serviceData = SettableInputValueDesiredAttributes(desiredState.value))
}

internal data class InputSelectDesiredAttributes(val option: String) : DesiredServiceData()

internal val INPUT_SELECT_RESOLVER: ServiceCallResolver<Enum<*>> = { desiredState ->
    DefaultResolvedServiceCommand(service = ServiceType.SELECT_OPTION, serviceData = InputSelectDesiredAttributes(desiredState.value.name))
}
