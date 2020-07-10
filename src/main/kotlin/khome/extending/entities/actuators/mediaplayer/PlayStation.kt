package khome.extending.entities.actuators.mediaplayer

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.communicating.ServiceType.TURN_OFF
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import java.time.Instant

typealias PlayStation = Actuator<PlayStationState, PlaystationAttributes>

enum class PlaystationService {
    SELECT_SOURCE, SEND_COMMAND
}

enum class PlaystationStateValue {
    @SerializedName("standby")
    STANDBY,

    @SerializedName("idle")
    IDLE,

    @SerializedName("playing")
    PLAYING
}

data class PlayStationState(override val value: PlaystationStateValue, val source: PlayStationSource? = null) :
    State<PlaystationStateValue>

data class PlaystationAttributes(
    val sourceList: List<String>,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

enum class PlayStationSource {
    @SerializedName("YouTube")
    YOUTUBE,

    @SerializedName("Spotify")
    SPOTIFY,

    @SerializedName("Fortnite")
    FORTNITE,

    @SerializedName("Call of Duty®: Modern Warfare®")
    CALL_OF_DUTY,

    @SerializedName("Grand Theft Auto V Premium Online Edition")
    GRAND_THEFT_AUTO
}

data class PlayStationServiceData(val source: PlayStationSource) : DesiredServiceData()

@Suppress("FunctionName")
fun KhomeApplication.PlayStation(objectId: String): PlayStation =
    MediaPlayer(objectId, ServiceCommandResolver { desiredState ->
        when (desiredState.value) {
            PlaystationStateValue.IDLE -> throw UnsupportedOperationException("PlayStation can not be turned on")
            PlaystationStateValue.STANDBY -> {
                DefaultResolvedServiceCommand(
                    service = TURN_OFF,
                    serviceData = EntityIdOnlyServiceData()
                )
            }
            PlaystationStateValue.PLAYING -> {
                desiredState.source?.let {
                    DefaultResolvedServiceCommand(
                        service = PlaystationService.SELECT_SOURCE,
                        serviceData = PlayStationServiceData(
                            source = desiredState.source
                        )
                    )
                }
                    ?: throw UnsupportedOperationException("You cannot set state to \"Playing\" without setting the source")
            }
        }
    })
