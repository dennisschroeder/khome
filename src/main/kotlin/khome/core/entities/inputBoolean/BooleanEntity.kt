package khome.core.entities.inputBoolean

import khome.core.entities.EntitySubject

abstract class BooleanEntity(name: String) : EntitySubject<String>("input_boolean", name) {
    val isOn get() = state.state == "on"
    val isOff get() = state.state == "off"
}
