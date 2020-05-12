package khome.core.entities.inputBoolean

import khome.core.entities.EntityId
import khome.core.entities.EntitySubject

abstract class BooleanEntity(name: String) : EntitySubject<String>(EntityId("input_boolean", name)) {
    val isOn get() = stateValue == "on"
    val isOff get() = stateValue == "off"
}
