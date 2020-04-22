package khome.core.entities.switchEntity

import khome.core.entities.AbstractEntity

abstract class AbstractSwitchEntity(name: String) : AbstractEntity<String>("switch", name) {
    open val isOn = newState.state == "on"
    open val isOff = newState.state == "off"
}
