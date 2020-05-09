package khome.core.entities.mediaPlayer

import khome.core.entities.EntitySubject
import khome.core.safeGet

abstract class MediaPlayerEntity(name: String) : EntitySubject<String>("media_player", name) {
    open val isOn get() = state == "on"
    open val isOff get() = state == "off"
    open val isIdle get() = state == "idle"
    open val isMute get() = if (isOn) attributes.safeGet("is_volume_muted") else false
}
