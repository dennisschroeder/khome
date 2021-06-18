package khome.extending.entities.actuators.mediaplayer

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.State
import khome.extending.entities.actuators.mediaplayer.MediaReceiverStateValue.IDLE
import khome.extending.entities.actuators.mediaplayer.MediaReceiverStateValue.OFF
import khome.extending.entities.actuators.mediaplayer.MediaReceiverStateValue.PAUSED
import khome.extending.entities.actuators.mediaplayer.MediaReceiverStateValue.PLAYING
import khome.extending.entities.actuators.mediaplayer.MediaReceiverStateValue.UNAVAILABLE
import khome.extending.entities.actuators.mediaplayer.MediaReceiverStateValue.UNKNOWN
import khome.extending.entities.actuators.onStateValueChangedFrom
import khome.extending.entities.actuators.stateValueChangedFrom
import khome.observability.Switchable
import khome.values.AlbumName
import khome.values.AppId
import khome.values.AppName
import khome.values.Artist
import khome.values.EntityPicture
import khome.values.FriendlyName
import khome.values.MediaContentId
import khome.values.MediaContentType
import khome.values.MediaDuration
import khome.values.MediaPosition
import khome.values.MediaTitle
import khome.values.Mute
import khome.values.ObjectId
import khome.values.UserId
import khome.values.VolumeLevel
import khome.values.service
import java.time.Instant

typealias MediaReceiver = MediaPlayer<MediaReceiverState, MediaReceiverAttributes>

@Suppress("FunctionName")
fun KhomeApplication.MediaReceiver(objectId: ObjectId): MediaReceiver =
    MediaPlayer(
        objectId,
        ServiceCommandResolver { desiredState ->
            when (desiredState.value) {
                IDLE -> {
                    desiredState.isVolumeMuted?.let { isMuted ->
                        DefaultResolvedServiceCommand(
                            service = "volume_mute".service,
                            serviceData = MediaReceiverDesiredServiceData(
                                isVolumeMuted = isMuted
                            )
                        )
                    } ?: desiredState.volumeLevel?.let { volumeLevel ->
                        DefaultResolvedServiceCommand(
                            service = "volume_set".service,
                            serviceData = MediaReceiverDesiredServiceData(
                                volumeLevel = volumeLevel
                            )
                        )
                    } ?: DefaultResolvedServiceCommand(
                        service = "turn_on".service,
                        serviceData = EntityIdOnlyServiceData()
                    )
                }

                PAUSED ->
                    desiredState.volumeLevel?.let { volumeLevel ->
                        DefaultResolvedServiceCommand(
                            service = "volume_set".service,
                            serviceData = MediaReceiverDesiredServiceData(
                                volumeLevel = volumeLevel
                            )
                        )
                    } ?: desiredState.mediaPosition?.let { position ->
                        DefaultResolvedServiceCommand(
                            service = "seek_position".service,
                            serviceData = MediaReceiverDesiredServiceData(
                                seekPosition = position
                            )
                        )
                    } ?: desiredState.isVolumeMuted?.let { isMuted ->
                        DefaultResolvedServiceCommand(
                            service = "volume_mute".service,
                            serviceData = MediaReceiverDesiredServiceData(
                                isVolumeMuted = isMuted
                            )
                        )
                    } ?: DefaultResolvedServiceCommand(
                        service = "media_pause".service,
                        serviceData = EntityIdOnlyServiceData()
                    )

                PLAYING ->
                    desiredState.mediaPosition?.let { position ->
                        DefaultResolvedServiceCommand(
                            service = "seek_position".service,
                            serviceData = MediaReceiverDesiredServiceData(
                                seekPosition = position
                            )
                        )
                    } ?: desiredState.isVolumeMuted?.let { isMuted ->
                        DefaultResolvedServiceCommand(
                            service = "volume_mute".service,
                            serviceData = MediaReceiverDesiredServiceData(
                                isVolumeMuted = isMuted
                            )
                        )
                    } ?: desiredState.volumeLevel?.let { volumeLevel ->
                        DefaultResolvedServiceCommand(
                            service = "volume_set".service,
                            serviceData = MediaReceiverDesiredServiceData(
                                volumeLevel = volumeLevel
                            )
                        )
                    } ?: DefaultResolvedServiceCommand(
                        service = "media_play".service,
                        serviceData = EntityIdOnlyServiceData()
                    )

                OFF -> {
                    DefaultResolvedServiceCommand(
                        service = "turn_off".service,
                        serviceData = EntityIdOnlyServiceData()
                    )
                }

                UNKNOWN -> throw IllegalStateException("State cannot be changed to UNKNOWN")
                UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
            }
        }
    )

data class MediaReceiverState(
    override val value: MediaReceiverStateValue,
    val volumeLevel: VolumeLevel? = null,
    val isVolumeMuted: Mute? = null,
    val mediaPosition: MediaPosition? = null
) : State<MediaReceiverStateValue>

enum class MediaReceiverStateValue {
    @SerializedName("unknown")
    UNKNOWN,

    @SerializedName("unavailable")
    UNAVAILABLE,

    @SerializedName("off")
    OFF,

    @SerializedName("idle")
    IDLE,

    @SerializedName("playing")
    PLAYING,

    @SerializedName("paused")
    PAUSED
}

data class MediaReceiverAttributes(
    val mediaContentId: MediaContentId?,
    val mediaTitle: MediaTitle?,
    val mediaArtist: Artist?,
    val mediaAlbumName: AlbumName?,
    val mediaContentType: MediaContentType?,
    val mediaDuration: MediaDuration?,
    val mediaPositionUpdatedAt: Instant?,
    val appId: AppId?,
    val appName: AppName?,
    val entityPicture: EntityPicture,
    override val userId: UserId?,
    override val friendlyName: FriendlyName,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class MediaReceiverDesiredServiceData(
    val isVolumeMuted: Mute? = null,
    val volumeLevel: VolumeLevel? = null,
    val seekPosition: MediaPosition? = null
) : DesiredServiceData()

val MediaReceiver.isOff
    get() = actualState.value == OFF

val MediaReceiver.isIdle
    get() = actualState.value == IDLE

val MediaReceiver.isPlaying
    get() = actualState.value == PLAYING

val MediaReceiver.isOn
    get() = actualState.value != OFF || actualState.value != UNAVAILABLE

val MediaReceiver.isPaused
    get() = actualState.value == PAUSED

fun MediaReceiver.turnOn() {
    desiredState = MediaReceiverState(value = IDLE)
}

fun MediaReceiver.turnOff() {
    desiredState = MediaReceiverState(value = OFF)
}

fun MediaReceiver.play() {
    desiredState = MediaReceiverState(value = PLAYING)
}

fun MediaReceiver.pause() {
    desiredState = MediaReceiverState(value = PAUSED)
}

fun MediaReceiver.setVolumeTo(level: VolumeLevel) {
    if (actualState.value == UNAVAILABLE || actualState.value == OFF)
        throw RuntimeException("Volume can not be set when MediaReceiver is ${actualState.value}")

    desiredState = MediaReceiverState(value = actualState.value, volumeLevel = level)
}

fun MediaReceiver.muteVolume() {
    desiredState = MediaReceiverState(value = actualState.value, isVolumeMuted = Mute.TRUE)
}

fun MediaReceiver.unMuteVolume() {
    desiredState = MediaReceiverState(value = actualState.value, isVolumeMuted = Mute.FALSE)
}

fun MediaReceiver.onPlaybackStarted(f: MediaReceiver.(Switchable) -> Unit) =
    onStateValueChangedFrom(IDLE to PLAYING, f)

fun MediaReceiver.onPlaybackStopped(f: MediaReceiver.(Switchable) -> Unit) =
    attachObserver {
        if (stateValueChangedFrom(PLAYING to IDLE) ||
            stateValueChangedFrom(PLAYING to OFF) ||
            stateValueChangedFrom(PAUSED to OFF) ||
            stateValueChangedFrom(PAUSED to IDLE)
        ) {
            f(this, it)
        }
    }

fun MediaReceiver.onPlaybackPaused(f: MediaReceiver.(Switchable) -> Unit) =
    onStateValueChangedFrom(PLAYING to PAUSED, f)

fun MediaReceiver.onPlaybackResumed(f: MediaReceiver.(Switchable) -> Unit) =
    onStateValueChangedFrom(PAUSED to PLAYING, f)

fun MediaReceiver.onTurnedOn(f: MediaReceiver.(Switchable) -> Unit) =
    onStateValueChangedFrom(UNKNOWN to IDLE, f)

fun MediaReceiver.onTurnedOff(f: MediaReceiver.(Switchable) -> Unit) =
    onStateValueChangedFrom(IDLE to OFF, f)
