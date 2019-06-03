package khome.calling

import khome.listening.getEntityInstance
import khome.core.entities.mediaPlayer.AbstractMediaPlayerEntity

inline fun <reified Entity : AbstractMediaPlayerEntity> ServiceCaller.setVolume(level: Float) {
    val entity = getEntityInstance<Entity>()
    setVolume(entity.id, level)
}

fun ServiceCaller.setVolume(entityId: String, level: Float) {
    mediaPlayer {
        this.entityId = entityId
        service = MediaPlayerServices.VOLUME_SET
        volumeLevel = level
    }
}

inline fun <reified Entity : AbstractMediaPlayerEntity> ServiceCaller.volumeDown() {
    val entity = getEntityInstance<Entity>()
    volumeDown(entity.id)
}

fun ServiceCaller.volumeDown(entityId: String) {
    mediaPlayer {
        this.entityId = entityId
        service = MediaPlayerServices.VOLUME_DOWN
    }
}

inline fun <reified Entity : AbstractMediaPlayerEntity> ServiceCaller.volumeUp() {
    val entity = getEntityInstance<Entity>()
    volumeUp(entity.id)
}

fun ServiceCaller.volumeUp(entityId: String) {
    mediaPlayer {
        this.entityId = entityId
        service = MediaPlayerServices.VOLUME_UP
    }
}

inline fun <reified Entity : AbstractMediaPlayerEntity> ServiceCaller.unMute() {
    val entity = getEntityInstance<Entity>()
    unMute(entity.id)
}

fun ServiceCaller.unMute(entityId: String) {
    mediaPlayer {
        this.entityId = entityId
        service = MediaPlayerServices.VOLUME_MUTE
        isVolumeMuted = false
    }
}

inline fun <reified Entity : AbstractMediaPlayerEntity> ServiceCaller.mute() {
    val entity = getEntityInstance<Entity>()
    mute(entity.id)
}

fun ServiceCaller.mute(entityId: String) {
    mediaPlayer {
        this.entityId = entityId
        service = MediaPlayerServices.VOLUME_MUTE
        isVolumeMuted = true
    }
}

fun ServiceCaller.mediaPlayer(init: MediaData.() -> Unit) {
    domain = Domain.MEDIA_PLAYER
    serviceData = MediaData(
        null,
        null,
        null,
        null,
        null,
        null
    ).apply(init)
}

data class MediaData(
    override var entityId: String?,
    var volumeLevel: Float?,
    var isVolumeMuted: Boolean?,
    var mediaContentId: String?,
    var mediaContentType: MediaContentType?,
    var source: String?

) : ServiceDataInterface

enum class MediaContentType {
    MUSIC, TVSHOW, VIDEO, EPISODE, CHANNEL, PLAYLIST
}

enum class MediaPlayerServices : ServiceInterface {
    TURN_ON, TURN_OFF, TOGGLE, VOLUME_UP, VOLUME_DOWN, VOLUME_SET, VOLUME_MUTE, PLAY_MEDIA, BAR
}