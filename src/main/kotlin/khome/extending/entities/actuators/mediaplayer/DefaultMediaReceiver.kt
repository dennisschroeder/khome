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
import khome.extending.entities.actuators.mediaplayer.DefaultMediaReceiverValue.*
import java.time.Instant

typealias DefaultMediaReceiver = MediaPlayer<DefaultMediaReceiverState, DefaultMediaReceiverAttributes>

@Suppress("FunctionName")
fun KhomeApplication.DefaultMediaReceiver(objectId: String): DefaultMediaReceiver =
    MediaPlayer(objectId, ServiceCommandResolver { desiredState ->
        when(desiredState.value) {
            IDLE -> {
                desiredState.isVolumeMuted?.let { isMuted ->
                    DefaultResolvedServiceCommand(
                        service = MediaPlayerService.VOLUME_MUTE,
                        serviceData = DefaultMediaReceiverDesiredServiceData(
                            isVolumeMuted = isMuted
                        )
                    )
                } ?: desiredState.volumeLevel?.let { volumeLevel ->
                    DefaultResolvedServiceCommand(
                        service = MediaPlayerService.VOLUME_SET,
                        serviceData = DefaultMediaReceiverDesiredServiceData(
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
                    service = MediaPlayerService.VOLUME_SET,
                    serviceData = DefaultMediaReceiverDesiredServiceData(
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

data class DefaultMediaReceiverState(
    override val value: DefaultMediaReceiverValue,
    val volumeLevel: Double? = null,
    val isVolumeMuted: Boolean? = null,
    val mediaPosition: Double? = null
) : State<DefaultMediaReceiverValue>

enum class DefaultMediaReceiverValue {
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

data class DefaultMediaReceiverAttributes(
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

data class DefaultMediaReceiverDesiredServiceData(
    val isVolumeMuted: Boolean? = null,
    val volumeLevel: Double? = null,
    val seekPosition: Double? = null
) : DesiredServiceData()
