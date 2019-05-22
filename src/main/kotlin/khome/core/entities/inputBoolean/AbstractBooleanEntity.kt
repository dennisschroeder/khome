package khome.core.entities.inputBoolean

import khome.core.entities.AbstractEntity

abstract class AbstractBooleanEntity(inputBooleanEntityName: String) : AbstractEntity("input_boolean", inputBooleanEntityName) {
    val isOn get() = getStateValue<String>() == "on"
    val isOff get() = getStateValue<String>() == "off"
}