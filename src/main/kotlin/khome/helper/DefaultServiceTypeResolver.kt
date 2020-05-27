package khome.helper

import khome.communicating.ServiceType
import khome.communicating.ServiceTypeResolver

val SWITCHABLE_RESOLVER: ServiceTypeResolver<Switchable> = { desiredState ->
    when (desiredState.value) {
        Switchable.ON -> ServiceType.TURN_ON
        Switchable.OFF -> ServiceType.TURN_OFF
    }
}
