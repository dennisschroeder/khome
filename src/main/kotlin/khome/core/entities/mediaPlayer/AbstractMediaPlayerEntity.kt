package khome.core.entities.mediaPlayer

import khome.core.entities.AbstractEntity

abstract class AbstractMediaPlayerEntity(name: String) : AbstractEntity<String>("media_player", name) {
    val isOn = stateValue == "on"
    val isOff = stateValue == "off"
    val isIdle = stateValue == "idle"
    val isMute get() = if (isOn) getAttributeValue("is_volume_muted") else false
    val volume get() = if (isOn) getAttributeValue("volume_level") else 0.00
}