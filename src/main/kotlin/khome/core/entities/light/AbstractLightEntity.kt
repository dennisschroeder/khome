package khome.core.entities.light

import khome.core.entities.AbstractEntity

abstract class AbstractLightEntity(name: String) : AbstractEntity("light", name) {
    val isOn = getStateValue<String>() == "on"
    val isOff = getStateValue<String>() == "off"
}