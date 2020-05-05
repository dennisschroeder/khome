package khome.core.entities.light

import khome.core.entities.EntitySubject
import khome.core.hasStateChangedAfter

abstract class LightEntity(name: String) : EntitySubject<String>("light", name) {
    open val isOn get() = state.state == "on"
    open val isOff get() = state.state == "off"

    open suspend fun isStillOnAfter(millis: Long): Boolean =
        state.state == "on" && state.hasStateChangedAfter(millis = millis)

    open suspend fun isStillOffAfter(millis: Long): Boolean =
        state.state == "off" && state.hasStateChangedAfter(millis = millis)
}
