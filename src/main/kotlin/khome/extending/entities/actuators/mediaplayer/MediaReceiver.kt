package khome.extending.entities.actuators.mediaplayer

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.communicating.ServiceType
import khome.entities.Attributes
import khome.entities.State
import khome.extending.entities.actuators.mediaplayer.MediaReceiverValue.IDLE
import khome.extending.entities.actuators.mediaplayer.MediaReceiverValue.OFF
import khome.extending.entities.actuators.mediaplayer.MediaReceiverValue.PAUSED
import khome.extending.entities.actuators.mediaplayer.MediaReceiverValue.PLAYING
import khome.extending.entities.actuators.mediaplayer.MediaReceiverValue.UNKNOWN
import khome.extending.entities.actuators.stateValueChangedFrom
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineScope
import java.time.Instant

typealias MediaReceiver = MediaPlayer<MediaReceiverState, MediaReceiverAttributes>

@Suppress("FunctionName")
fun KhomeApplication.MediaReceiver(objectId: String): MediaReceiver =
    MediaPlayer(objectId, ServiceCommandResolver { desiredState ->
        when (desiredState.value) {
            IDLE -> {
                desiredState.isVolumeMuted?.let { isMuted ->
                    DefaultResolvedServiceCommand(
                        service = MediaPlayerService.VOLUME_MUTE,
                        serviceData = MediaReceiverDesiredServiceData(
                            isVolumeMuted = isMuted
                        )
                    )
                } ?: desiredState.volumeLevel?.let { volumeLevel ->
                    DefaultResolvedServiceCommand(
                        service = MediaPlayerService.VOLUME_SET,
                        serviceData = MediaReceiverDesiredServiceData(
                            volumeLevel = volumeLevel
                        )
                    )
                } ?: DefaultResolvedServiceCommand(
                    service = ServiceType.TURN_ON,
                    serviceData = EntityIdOnlyServiceData()
                )
            }

            PAUSED -> DefaultResolvedServiceCommand(
                service = MediaPlayerService.MEDIA_PAUSE,
                serviceData = EntityIdOnlyServiceData()
            )

            PLAYING -> desiredState.mediaPosition?.let { position ->
                DefaultResolvedServiceCommand(
                    service = MediaPlayerService.SEEK_POSITION,
                    serviceData = MediaReceiverDesiredServiceData(
                        seekPosition = position
                    )
                )
            } ?: DefaultResolvedServiceCommand(
                service = MediaPlayerService.MEDIA_PLAY,
                serviceData = EntityIdOnlyServiceData()
            )

            OFF -> {
                DefaultResolvedServiceCommand(
                    service = ServiceType.TURN_OFF,
                    serviceData = EntityIdOnlyServiceData()
                )
            }

            UNKNOWN -> throw IllegalStateException("State cannot be changed to UNKNOWN")
        }
    })

data class MediaReceiverState(
    override val value: MediaReceiverValue,
    val volumeLevel: Double? = null,
    val isVolumeMuted: Boolean? = null,
    val mediaPosition: Double? = null
) : State<MediaReceiverValue>

enum class MediaReceiverValue {
    @SerializedName("unknown")
    UNKNOWN,

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
    val mediaContentId: String?,
    val mediaTitle: String?,
    val mediaArtist: String?,
    val mediaAlbumName: String?,
    val mediaContentType: String?,
    val mediaDuration: Double?,
    val mediaPositionUpdatedAt: Instant?,
    val appId: String?,
    val appName: String?,
    val entityPicture: String,
    override val userId: String?,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class MediaReceiverDesiredServiceData(
    val isVolumeMuted: Boolean? = null,
    val volumeLevel: Double? = null,
    val seekPosition: Double? = null
) : DesiredServiceData()

val MediaReceiver.isOFF
    get() = actualState.value == OFF

val MediaReceiver.isIdle
    get() = actualState.value == IDLE

val MediaReceiver.isPlaying
    get() = actualState.value == PLAYING

val MediaReceiver.isON
    get() = actualState.value == IDLE || actualState.value == PLAYING

val MediaReceiver.isPaused
    get() = actualState.value == PAUSED

fun MediaReceiver.onPlaybackStarted(f: MediaReceiver.(Switchable) -> Unit) =
    attachObserver {
        if (stateValueChangedFrom(IDLE to PLAYING))
            f(this, it)
    }

fun MediaReceiver.onPlaybackStartedAsync(f: suspend MediaReceiver.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, coroutineScope ->
        if (stateValueChangedFrom(IDLE to PLAYING))
            f(this, observer, coroutineScope)
    }

fun MediaReceiver.onPlaybackStopped(f: MediaReceiver.(Switchable) -> Unit) =
    attachObserver {
        if (stateValueChangedFrom(PLAYING to IDLE) ||
            stateValueChangedFrom(PLAYING to OFF)
        ) {
            f(this, it)
        }
    }

fun MediaReceiver.onPlaybackStoppedAsync(f: suspend MediaReceiver.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, coroutineScope ->
        if (
            stateValueChangedFrom(PLAYING to IDLE) ||
            stateValueChangedFrom(PLAYING to OFF)
        ) {
            f(this, observer, coroutineScope)
        }
    }

fun MediaReceiver.onPlaybackPaused(f: MediaReceiver.(Switchable) -> Unit) =
    attachObserver {
        if (stateValueChangedFrom(PLAYING to PAUSED))
            f(this, it)
    }

fun MediaReceiver.onPlaybackPausedAsync(f: suspend MediaReceiver.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, coroutineScope ->
        if (stateValueChangedFrom(PLAYING to PAUSED))
            f(this, observer, coroutineScope)
    }
