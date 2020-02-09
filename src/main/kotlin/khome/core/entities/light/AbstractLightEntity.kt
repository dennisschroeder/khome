package khome.core.entities.light

import khome.core.entities.AbstractEntity

abstract class AbstractLightEntity(name: String) : AbstractEntity<String>("light", name) {
    val isOn get() = stateValue == "on"
    val isOff get() = stateValue == "off"

    suspend fun isStillOnAfter(millis: Long): Boolean =
        stateValue == "on" && hasStateChangedAfter(millis = millis)

    suspend fun isStillOffAfter(millis: Long): Boolean =
        stateValue == "off" && hasStateChangedAfter(millis = millis)
}
