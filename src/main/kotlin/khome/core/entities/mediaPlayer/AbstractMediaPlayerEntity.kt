package khome.core.entities.mediaPlayer

import khome.core.entities.AbstractEntity
import khome.core.entities.getAttribute

abstract class AbstractMediaPlayerEntity(name: String) : AbstractEntity<String>("media_player", name) {
    open val isOn get() = newState.state == "on"
    open val isOff get() = newState.state == "off"
    open val isIdle get() = newState.state == "idle"
    open val isMute get() = if (isOn) newState.getAttribute("is_volume_muted") else false
}
