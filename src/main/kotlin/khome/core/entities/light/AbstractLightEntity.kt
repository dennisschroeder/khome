package khome.core.entities.light

import khome.core.entities.AbstractEntity

abstract class AbstractLightEntity(lightEntityName: String) : AbstractEntity("light", lightEntityName) {
    val isOn = getStateValue<String>() == "on"
    val isOff = getStateValue<String>() == "off"
}