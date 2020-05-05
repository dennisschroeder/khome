package khome.core.entities.mediaPlayer

import khome.core.entities.EntitySubject
import khome.core.getAttribute

abstract class MediaPlayerEntity(name: String) : EntitySubject<String>("media_player", name) {
    open val isOn get() = state.state == "on"
    open val isOff get() = state.state == "off"
    open val isIdle get() = state.state == "idle"
    open val isMute get() = if (isOn) state.getAttribute("is_volume_muted") else false
}
