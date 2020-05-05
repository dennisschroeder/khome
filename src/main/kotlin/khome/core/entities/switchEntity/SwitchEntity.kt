package khome.core.entities.switchEntity

import khome.core.entities.EntitySubject

abstract class SwitchEntity(name: String) : EntitySubject<String>("switch", name) {
    open val isOn = state.state == "on"
    open val isOff = state.state == "off"
}
