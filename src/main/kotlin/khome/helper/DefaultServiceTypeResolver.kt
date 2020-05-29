package khome.helper

import khome.communicating.ServiceType
import khome.communicating.ServiceTypeResolver

val SWITCHABLE_VALUE_RESOLVER: ServiceTypeResolver<SwitchableValue> = { desiredState ->
    when (desiredState.value) {
        SwitchableValue.ON -> ServiceType.TURN_ON
        SwitchableValue.OFF -> ServiceType.TURN_OFF
    }
}
