package khome.extending.entities.actuators.mediaplayer

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.State
import khome.extending.entities.SwitchableValue
import khome.extending.entities.actuators.onStateValueChangedFrom
import khome.observability.Switchable
import khome.values.FriendlyName
import khome.values.MediaContentId
import khome.values.MediaContentType
import khome.values.MediaSource
import khome.values.MediaTitle
import khome.values.Mute
import khome.values.ObjectId
import khome.values.UserId
import khome.values.VolumeLevel
import khome.values.service
import java.time.Instant

typealias Television = MediaPlayer<TelevisionState, TelevisionAttributes>

@Suppress("FunctionName")
fun KhomeApplication.Television(objectId: ObjectId): Television =
    MediaPlayer(
        objectId,
        ServiceCommandResolver { desiredState ->
            when (desiredState.value) {
                SwitchableValue.ON -> {
                    desiredState.isVolumeMuted?.let { isMuted ->
                        DefaultResolvedServiceCommand(
                            service = "volume_mute".service,
                            serviceData = TelevisionDesiredServiceData(
                                isVolumeMuted = isMuted
                            )
                        )
                    } ?: desiredState.volumeLevel?.let { volumeLevel ->
                        DefaultResolvedServiceCommand(
                            service = "volume_set".service,
                            serviceData = TelevisionDesiredServiceData(
                                volumeLevel = volumeLevel
                            )
                        )
                    } ?: desiredState.source?.let { source ->
                        DefaultResolvedServiceCommand(
                            service = "volume_set".service,
                            serviceData = TelevisionDesiredServiceData(
                                source = source
                            )
                        )
                    } ?: DefaultResolvedServiceCommand(
                        service = "turn_on".service,
                        serviceData = EntityIdOnlyServiceData()
                    )
                }

                SwitchableValue.OFF -> {
                    DefaultResolvedServiceCommand(
                        service = "turn_off".service,
                        serviceData = EntityIdOnlyServiceData()
                    )
                }

                SwitchableValue.UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
            }
        }
    )

data class TelevisionState(
    override val value: SwitchableValue,
    val volumeLevel: VolumeLevel? = null,
    val isVolumeMuted: Mute? = null,
    val source: MediaSource? = null
) : State<SwitchableValue>

data class TelevisionAttributes(
    val mediaContentId: MediaContentId,
    val mediaTitle: MediaTitle,
    val mediaContentType: MediaContentType,
    override val userId: UserId?,
    override val friendlyName: FriendlyName,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class TelevisionDesiredServiceData(
    val isVolumeMuted: Mute? = null,
    val volumeLevel: VolumeLevel? = null,
    val source: MediaSource? = null
) : DesiredServiceData()

val Television.isOn
    get() = actualState.value == SwitchableValue.ON

val Television.isOff
    get() = actualState.value == SwitchableValue.OFF

val Television.isMuted
    get() = actualState.isVolumeMuted == Mute.TRUE

fun Television.turnOn() {
    desiredState = TelevisionState(value = SwitchableValue.ON)
}

fun Television.turnOff() {
    desiredState = TelevisionState(value = SwitchableValue.OFF)
}

fun Television.setVolumeTo(level: VolumeLevel) {
    desiredState = TelevisionState(value = SwitchableValue.ON, volumeLevel = level)
}

fun Television.muteVolume() {
    desiredState = TelevisionState(value = SwitchableValue.ON, isVolumeMuted = Mute.TRUE)
}

fun Television.unMuteVolume() {
    desiredState = TelevisionState(value = SwitchableValue.ON, isVolumeMuted = Mute.FALSE)
}

fun Television.setSource(source: MediaSource) {
    desiredState = TelevisionState(value = SwitchableValue.ON, source = source)
}

fun Television.onTurnedOn(f: Television.(Switchable) -> Unit) =
    onStateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON, f)

fun Television.onTurnedOff(f: Television.(Switchable) -> Unit) =
    onStateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF, f)
