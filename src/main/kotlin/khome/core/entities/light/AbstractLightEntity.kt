package khome.core.entities.light

import khome.core.entities.AbstractEntity
import khome.core.entities.hasStateChangedAfter

abstract class AbstractLightEntity(name: String) : AbstractEntity<String>("light", name) {
    val isOn get() = newState.state == "on"
    val isOff get() = newState.state == "off"

    suspend fun isStillOnAfter(millis: Long): Boolean =
        newState.state == "on" && newState.hasStateChangedAfter(millis = millis)

    suspend fun isStillOffAfter(millis: Long): Boolean =
        newState.state == "off" && newState.hasStateChangedAfter(millis = millis)
}
