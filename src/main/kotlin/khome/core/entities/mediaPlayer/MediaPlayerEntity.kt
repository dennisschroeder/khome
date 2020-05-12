package khome.core.entities.mediaPlayer

import khome.core.entities.EntityId
import khome.core.entities.EntitySubject
import khome.core.safeGet

abstract class MediaPlayerEntity(name: String) : EntitySubject<String>(EntityId("media_player", name)) {
    open val isOn get() = stateValue == "on"
    open val isOff get() = stateValue == "off"
    open val isIdle get() = stateValue == "idle"
    open val isMute get() = if (isOn) attributes.safeGet("is_volume_muted") else false
}
