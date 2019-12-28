package khome.calling

import khome.core.entities.mediaPlayer.AbstractMediaPlayerEntity

fun ServiceCall.setVolume(entity: AbstractMediaPlayerEntity, level: Float) =
    mediaPlayer {
        entityId = entity.id
        service = MediaPlayerServices.VOLUME_SET
        volumeLevel = level
    }

fun ServiceCall.volumeDown(entity: AbstractMediaPlayerEntity) =
    mediaPlayer {
        entityId = entity.id
        service = MediaPlayerServices.VOLUME_DOWN
    }

fun ServiceCall.volumeUp(entity: AbstractMediaPlayerEntity) =
    mediaPlayer {
        entityId = entity.id
        service = MediaPlayerServices.VOLUME_UP
    }

fun ServiceCall.unMute(entity: AbstractMediaPlayerEntity) =
    mediaPlayer {
        entityId = entity.id
        service = MediaPlayerServices.VOLUME_MUTE
        isVolumeMuted = false
    }

fun ServiceCall.mute(entity: AbstractMediaPlayerEntity) =
    mediaPlayer {
        entityId = entity.id
        service = MediaPlayerServices.VOLUME_MUTE
        isVolumeMuted = true
    }

fun ServiceCall.mediaPlayer(init: MediaData.() -> Unit) {
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
    TURN_ON, TURN_OFF, TOGGLE, VOLUME_UP, VOLUME_DOWN, VOLUME_SET, VOLUME_MUTE, PLAY_MEDIA
}
