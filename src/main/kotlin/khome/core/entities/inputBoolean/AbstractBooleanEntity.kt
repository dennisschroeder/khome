package khome.core.entities.inputBoolean

import khome.core.entities.AbstractEntity

abstract class AbstractBooleanEntity(name: String) : AbstractEntity<String>("input_boolean", name) {
    val isOn get() = newState.state == "on"
    val isOff get() = newState.state == "off"
}
