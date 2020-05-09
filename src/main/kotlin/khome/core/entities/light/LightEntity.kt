package khome.core.entities.light

import khome.core.entities.EntitySubject

abstract class LightEntity(name: String) : EntitySubject<String>("light", name) {
    open val isOn get() = state == "on"
    open val isOff get() = state == "off"
}
