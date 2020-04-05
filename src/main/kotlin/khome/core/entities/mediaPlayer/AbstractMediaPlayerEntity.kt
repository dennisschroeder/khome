package khome.core.entities.mediaPlayer

import khome.core.entities.AbstractEntity
import khome.core.entities.getAttribute

abstract class AbstractMediaPlayerEntity(name: String) : AbstractEntity<String>("media_player", name) {
    val isOn get() = newState.state == "on"
    val isOff get() = newState.state == "off"
    val isIdle get() = newState.state == "idle"
    val isMute get() = if (isOn) newState.getAttribute("is_volume_muted") else false
    val volume get() = if (isOn) newState.getAttribute("volume_level") else 0.00
}
