package khome.core.entities.light

import khome.core.entities.AbstractEntity
import khome.core.entities.hasStateChangedAfter

abstract class AbstractLightEntity(name: String) : AbstractEntity<String>("light", name) {
    open val isOn get() = newState.state == "on"
    open val isOff get() = newState.state == "off"

    open suspend fun isStillOnAfter(millis: Long): Boolean =
        newState.state == "on" && newState.hasStateChangedAfter(millis = millis)

    open suspend fun isStillOffAfter(millis: Long): Boolean =
        newState.state == "off" && newState.hasStateChangedAfter(millis = millis)
}
