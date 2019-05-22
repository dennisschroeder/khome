package khome.core.entities.inputBoolean

import khome.core.entities.AbstractEntity

abstract class AbstractBooleanEntity(name: String) : AbstractEntity("input_boolean", name) {
    val isOn get() = getStateValue<String>() == "on"
    val isOff get() = getStateValue<String>() == "off"
}