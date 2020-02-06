package khome.core.entities.light

import khome.core.entities.AbstractEntity

abstract class AbstractLightEntity(name: String) : AbstractEntity<String>("light", name) {
    val isOn get() = stateValue == "on"
    val isOff get() = stateValue == "off"
}
