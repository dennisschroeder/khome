package khome.core.entities.switchEntity

import khome.core.entities.AbstractEntity

abstract class AbstractSwitchEntity(name: String) : AbstractEntity<String>("switch", name) {
    val isOn = stateValue == "on"
    val isOff = stateValue == "off"
}