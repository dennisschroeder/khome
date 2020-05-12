package khome.core.entities.light

import khome.core.entities.EntityId
import khome.core.entities.EntitySubject

abstract class LightEntity(name: String) : EntitySubject<String>(EntityId("light", name)) {
    open val isOn get() = stateValue == "on"
    open val isOff get() = stateValue == "off"
}
