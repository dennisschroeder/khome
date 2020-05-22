package khome.calling

class TurnOnMediaPlayer :
    EntityIdOnlyServiceCall(HassDomain.MEDIA_PLAYER, MediaPlayerService.TURN_ON)

class TurnOffMediaPlayer :
    EntityIdOnlyServiceCall(HassDomain.MEDIA_PLAYER, MediaPlayerService.TURN_OFF)

class ToggleMediaPlayer :
    EntityIdOnlyServiceCall(HassDomain.MEDIA_PLAYER, MediaPlayerService.TOGGLE)

class SetVolume :
    ServiceCall(HassDomain.MEDIA_PLAYER, MediaPlayerService.VOLUME_SET) {
    val serviceData: VolumeData = VolumeData(null, null)
    fun volumeLevel(level: Float) = serviceData.apply { volumeLevel = level }
}

class VolumeDown :
    EntityIdOnlyServiceCall(HassDomain.MEDIA_PLAYER, MediaPlayerService.VOLUME_DOWN)

class VolumeUp :
    EntityIdOnlyServiceCall(HassDomain.MEDIA_PLAYER, MediaPlayerService.VOLUME_UP)

class MutePlayer :
    ServiceCall(HassDomain.MEDIA_PLAYER, MediaPlayerService.VOLUME_MUTE) {
    val serviceData: MuteData = MuteData(null, true)
    fun unMute() = serviceData.apply { isVolumeMuted = false }
}

class SelectSource :
    ServiceCall(HassDomain.MEDIA_PLAYER, MediaPlayerService.SELECT_SOURCE) {
    val serviceData: SourceData = SourceData(null, null)
    fun configure(builder: SourceData.() -> Unit) = serviceData.apply(builder)
}

class PlayMedia :
    ServiceCall(HassDomain.MEDIA_PLAYER, MediaPlayerService.PLAY_MEDIA) {
    val serviceData: ContentData = ContentData(null, null, null)
    fun configure(builder: ContentData.() -> Unit) = serviceData.apply(builder)
}

data class MuteData(
    var entityId: String?,
    var isVolumeMuted: Boolean
) : ServiceDataInterface

data class VolumeData(
    var entityId: String?,
    var volumeLevel: Float?
) : ServiceDataInterface

data class SourceData(
    var entityId: String?,
    var source: String?
) : ServiceDataInterface

data class ContentData(
    var entityId: String?,
    var mediaContentId: String?,
    var mediaContentType: MediaContentType?
) : ServiceDataInterface {

    enum class MediaContentType {
        IMAGE, MUSIC, TV_SHOW, VIDEO, EPISODE, CHANNEL, PLAYLIST
    }
}

enum class MediaPlayerService : ServiceInterface {
    TURN_ON, TURN_OFF, TOGGLE, VOLUME_UP, VOLUME_DOWN, VOLUME_SET, VOLUME_MUTE, PLAY_MEDIA, SELECT_SOURCE
}
