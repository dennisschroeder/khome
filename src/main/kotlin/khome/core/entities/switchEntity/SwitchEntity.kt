package khome.core.entities.switchEntity

import khome.core.entities.EntityId
import khome.core.entities.EntitySubject

abstract class SwitchEntity(name: String) : EntitySubject<String>(EntityId("switch", name)) {
    open val isOn = stateValue == "on"
    open val isOff = stateValue == "off"
}
