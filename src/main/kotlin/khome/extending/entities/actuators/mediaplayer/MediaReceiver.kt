package khome.extending.entities.actuators.mediaplayer

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.State
import khome.extending.entities.SwitchableValue
import khome.extending.entities.actuators.mediaplayer.MediaReceiverValue.IDLE
import khome.extending.entities.actuators.mediaplayer.MediaReceiverValue.OFF
import khome.extending.entities.actuators.mediaplayer.MediaReceiverValue.PAUSED
import khome.extending.entities.actuators.mediaplayer.MediaReceiverValue.PLAYING
import khome.extending.entities.actuators.mediaplayer.MediaReceiverValue.UNAVAILABLE
import khome.extending.entities.actuators.stateValueChangedFrom
import khome.observability.Switchable
import khome.values.FriendlyName
import khome.values.ObjectId
import khome.values.UserId
import khome.values.service
import java.time.Instant

typealias MediaReceiver = MediaPlayer<MediaReceiverState, MediaReceiverAttributes>

@Suppress("FunctionName")
fun KhomeApplication.MediaReceiver(objectId: ObjectId): MediaReceiver =
    MediaPlayer(objectId, ServiceCommandResolver { desiredState ->
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
                }
                ?: desiredState.volumeLevel?.let { volumeLevel ->
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

            UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
        }
    })

data class MediaReceiverState(
    override val value: MediaReceiverValue,
    val volumeLevel: Double? = null,
    val isVolumeMuted: Boolean? = null,
    val mediaPosition: Double? = null
) : State<MediaReceiverValue>

enum class MediaReceiverValue {
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
    override val userId: UserId?,
    override val friendlyName: FriendlyName,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class MediaReceiverDesiredServiceData(
    val isVolumeMuted: Boolean? = null,
    val volumeLevel: Double? = null,
    val seekPosition: Double? = null
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

fun MediaReceiver.setVolumeTo(level: Double) {
    if (actualState.value == UNAVAILABLE || actualState.value == OFF)
        throw RuntimeException("Volume can not be set when MediaReceiver is ${actualState.value}")

    desiredState = MediaReceiverState(value = actualState.value, volumeLevel = level)
}

fun MediaReceiver.muteVolume() {
    desiredState = MediaReceiverState(value = actualState.value, isVolumeMuted = true)
}

fun MediaReceiver.unMuteVolume() {
    desiredState = MediaReceiverState(value = actualState.value, isVolumeMuted = false)
}

fun MediaReceiver.onPlaybackStarted(f: MediaReceiver.(Switchable) -> Unit) =
    attachObserver {
        if (stateValueChangedFrom(IDLE to PLAYING))
            f(this, it)
    }

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
    attachObserver {
        if (stateValueChangedFrom(PLAYING to PAUSED))
            f(this, it)
    }

fun MediaReceiver.onPlaybackResumed(f: MediaReceiver.(Switchable) -> Unit) =
    attachObserver {
        if (stateValueChangedFrom(PAUSED to PLAYING))
            f(this, it)
    }

fun MediaReceiver.onTurnedOn(f: MediaReceiver.(Switchable) -> Unit) =
    attachObserver {
        if (stateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON))
            f(this, it)
    }
