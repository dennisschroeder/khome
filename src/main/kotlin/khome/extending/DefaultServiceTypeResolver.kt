package khome.extending

import khome.communicating.ServiceType
import khome.communicating.ServiceTypeResolver

internal val SWITCHABLE_VALUE_RESOLVER: ServiceTypeResolver<SwitchableValue> = { desiredState ->
    when (desiredState.value) {
        SwitchableValue.ON -> ServiceType.TURN_ON
        SwitchableValue.OFF -> ServiceType.TURN_OFF
    }
}

internal val INPUT_TEXT_RESOLVER : ServiceTypeResolver<String> = { ServiceType.SET_VALUE }
