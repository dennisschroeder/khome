package khome.core.entities.inputBoolean

import khome.core.entities.AbstractEntity

abstract class AbstractBooleanEntity(name: String) : AbstractEntity<String>("input_boolean", name) {
    val isOn get() = stateValue == "on"
    val isOff get() = stateValue == "off"
}