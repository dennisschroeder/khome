package khome.calling

import khome.core.entities.mediaPlayer.AbstractMediaPlayerEntity

abstract class TurnOnMediaPlayer(entity: AbstractMediaPlayerEntity) :
    ServiceCall(Domain.MEDIA_PLAYER, MediaPlayerService.TURN_ON) {
    override val serviceData: EntityId = EntityId(entity.id)
}

abstract class TurnOffMediaPlayer(entity: AbstractMediaPlayerEntity) :
    ServiceCall(Domain.MEDIA_PLAYER, MediaPlayerService.TURN_OFF) {
    override val serviceData: EntityId = EntityId(entity.id)
}

abstract class ToggleMediaPlayer(entity: AbstractMediaPlayerEntity) :
    ServiceCall(Domain.MEDIA_PLAYER, MediaPlayerService.TOGGLE) {
    override val serviceData: EntityId = EntityId(entity.id)
}

abstract class SetVolume(entity: AbstractMediaPlayerEntity) :
    ServiceCall(Domain.MEDIA_PLAYER, MediaPlayerService.VOLUME_SET) {
    override val serviceData: VolumeData = VolumeData(entity.id)
    fun serviceData(builder: VolumeData.() -> Unit) = serviceData.apply(builder)
}

abstract class VolumeDown(entity: AbstractMediaPlayerEntity) :
    ServiceCall(Domain.MEDIA_PLAYER, MediaPlayerService.VOLUME_DOWN) {
    override val serviceData: EntityId = EntityId(entity.id)
}

abstract class VolumeUp(entity: AbstractMediaPlayerEntity) :
    ServiceCall(Domain.MEDIA_PLAYER, MediaPlayerService.VOLUME_UP) {
    override val serviceData: EntityId = EntityId(entity.id)
}

abstract class MutePlayer(entity: AbstractMediaPlayerEntity) :
    ServiceCall(Domain.MEDIA_PLAYER, MediaPlayerService.VOLUME_MUTE) {
    override val serviceData: MuteData = MuteData(entity.id)
    fun serviceData(builder: MuteData.() -> Unit) = serviceData.apply(builder)
}

abstract class SelectSource(entity: AbstractMediaPlayerEntity) :
    ServiceCall(Domain.MEDIA_PLAYER, MediaPlayerService.SELECT_SOURCE) {
    override val serviceData: SourceData = SourceData(entity.id)
    fun serviceData(builder: SourceData.() -> Unit) = serviceData.apply(builder)
}

abstract class PlayMedia(entity: AbstractMediaPlayerEntity) :
    ServiceCall(Domain.MEDIA_PLAYER, MediaPlayerService.PLAY_MEDIA) {
    override val serviceData: ContentData = ContentData(entity.id)
    fun serviceData(builder: ContentData.() -> Unit) = serviceData.apply(builder)
}

data class MuteData(
    var entityId: String?,
    var isVolumeMuted: Boolean? = null
) : ServiceDataInterface

data class VolumeData(
    var entityId: String?,
    var volumeLevel: Float? = null
) : ServiceDataInterface

data class SourceData(
    var entityId: String?,
    var source: String? = null
) : ServiceDataInterface

data class ContentData(
    private val entityId: String?,
    var mediaContentId: String? = null,
    var mediaContentType: MediaContentType? = null
) : ServiceDataInterface {

    enum class MediaContentType {
        IMAGE, MUSIC, TV_SHOW, VIDEO, EPISODE, CHANNEL, PLAYLIST
    }
}

enum class MediaPlayerService : ServiceInterface {
    TURN_ON, TURN_OFF, TOGGLE, VOLUME_UP, VOLUME_DOWN, VOLUME_SET, VOLUME_MUTE, PLAY_MEDIA, SELECT_SOURCE
}
