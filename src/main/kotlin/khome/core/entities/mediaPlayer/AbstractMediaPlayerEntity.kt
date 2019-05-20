package khome.core.entities.mediaPlayer

import khome.core.entities.AbstractEntity

abstract class AbstractMediaPlayerEntity(medidPlayerEntityName: String) : AbstractEntity("media_player", medidPlayerEntityName) {
    val isOn = getStateValue<String>() == "on"
    val isOff = getStateValue<String>() == "off"
    val isIdle = getStateValue<String>() == "idle"
    val isMute get() = if (isOn) getAttributeValue("is_volume_muted") else false
    val volume get() = if (isOn) getAttributeValue("volume_level") else 0.00
}