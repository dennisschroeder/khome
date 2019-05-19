package khome.core.entities.mediaPlayer

import khome.core.entities.AbstractEntity

abstract class AbstractMediaPlayerEntity(medidPlayerEntityName: String) : AbstractEntity("media_player", medidPlayerEntityName) {
    val isOn = getStateValue<String>() == "on"
    val isOff = getStateValue<String>() == "off"
    val isIdle = getStateValue<String>() == "idle"
    val isMute = getAttributeValue<Boolean>("is_volume_muted")
    val volume = getAttributeValue<Double>("volume_level")
}