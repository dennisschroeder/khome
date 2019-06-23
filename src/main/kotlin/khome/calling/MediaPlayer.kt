package khome.calling

import khome.core.entities.mediaPlayer.AbstractMediaPlayerEntity

fun ServiceCaller.setVolume(entity: AbstractMediaPlayerEntity, level: Float) =
    mediaPlayer {
        entityId = entity.id
        service = MediaPlayerServices.VOLUME_SET
        volumeLevel = level
    }


fun ServiceCaller.volumeDown(entity: AbstractMediaPlayerEntity) =
    mediaPlayer {
        entityId = entity.id
        service = MediaPlayerServices.VOLUME_DOWN
    }

fun ServiceCaller.volumeUp(entity: AbstractMediaPlayerEntity) =
    mediaPlayer {
        this.entityId = entityId
        service = MediaPlayerServices.VOLUME_UP
    }

fun ServiceCaller.unMute(entity: AbstractMediaPlayerEntity) =
    mediaPlayer {
        this.entityId = entityId
        service = MediaPlayerServices.VOLUME_MUTE
        isVolumeMuted = false
    }

fun ServiceCaller.mute(entity: AbstractMediaPlayerEntity) =
    mediaPlayer {
        this.entityId = entityId
        service = MediaPlayerServices.VOLUME_MUTE
        isVolumeMuted = true
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