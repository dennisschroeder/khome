package khome.core.entities.switchEntity

import khome.core.entities.AbstractEntity

abstract class AbstractSwitchEntity(name: String) : AbstractEntity<String>("switch", name) {
    val isOn = newState.state == "on"
    val isOff = newState.state == "off"
}
