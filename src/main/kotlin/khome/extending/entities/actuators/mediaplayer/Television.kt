package khome.extending.entities.actuators.mediaplayer

import khome.KhomeApplication
import khome.StateAndAttributesHistorySnapshot
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.communicating.ServiceType
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.SwitchableValue
import java.time.Instant

typealias Television = Actuator<TelevisionState, TelevisionAttributes>
typealias TelevisionSnapshot = StateAndAttributesHistorySnapshot<TelevisionState, TelevisionAttributes>

data class TelevisionState(
    override val value: SwitchableValue,
    val volumeLevel: Double? = null,
    val isVolumeMuted: Boolean? = null,
    val source: String? = null
) : State<SwitchableValue>

data class TelevisionAttributes(
    val mediaContentId: String,
    val mediaTitle: String,
    val mediaContentType: String,
    override val userId: String?,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class TelevisionDesiredServiceData(
    val isVolumeMuted: Boolean? = null,
    val volumeLevel: Double? = null,
    val source: String? = null
) : DesiredServiceData()

enum class TelevisionService {
    VOLUME_MUTE, VOLUME_SET, SELECT_SOURCE
}

val Television.turnedOn
    get() = actualState.value == SwitchableValue.ON &&
            history[1].state.value == SwitchableValue.OFF

val Television.turnedOff
    get() = actualState.value == SwitchableValue.OFF &&
        history[1].state.value == SwitchableValue.ON

@Suppress("FunctionName")
fun KhomeApplication.Television(objectId: String): Television =
    MediaPlayer(objectId, ServiceCommandResolver { desiredState ->
        when (desiredState.value) {
            SwitchableValue.ON -> {
                desiredState.isVolumeMuted?.let { isMuted ->
                    DefaultResolvedServiceCommand(
                        service = TelevisionService.VOLUME_MUTE,
                        serviceData = TelevisionDesiredServiceData(
                            isVolumeMuted = isMuted
                        )
                    )
                } ?: desiredState.volumeLevel?.let { volumeLevel ->
                    DefaultResolvedServiceCommand(
                        service = TelevisionService.VOLUME_SET,
                        serviceData = TelevisionDesiredServiceData(
                            volumeLevel = volumeLevel
                        )
                    )
                } ?: desiredState.source?.let { source ->
                    DefaultResolvedServiceCommand(
                        service = TelevisionService.SELECT_SOURCE,
                        serviceData = TelevisionDesiredServiceData(
                            source = source
                        )
                    )
                } ?: DefaultResolvedServiceCommand(
                    service = ServiceType.TURN_ON,
                    serviceData = EntityIdOnlyServiceData()
                )
            }

            SwitchableValue.OFF -> {
                DefaultResolvedServiceCommand(
                    service = ServiceType.TURN_OFF,
                    serviceData = EntityIdOnlyServiceData()
                )
            }
        }
    })
