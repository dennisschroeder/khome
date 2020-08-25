package khome.extending.entities.actuators.mediaplayer

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.communicating.ServiceType
import khome.entities.Attributes
import khome.entities.State
import khome.extending.entities.SwitchableValue
import khome.extending.entities.actuators.stateValueChangedFrom
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineScope
import java.time.Instant

typealias Television = MediaPlayer<TelevisionState, TelevisionAttributes>

@Suppress("FunctionName")
fun KhomeApplication.Television(objectId: String): Television =
    MediaPlayer(objectId, ServiceCommandResolver { desiredState ->
        when (desiredState.value) {
            SwitchableValue.ON -> {
                desiredState.isVolumeMuted?.let { isMuted ->
                    DefaultResolvedServiceCommand(
                        service = MediaPlayerService.VOLUME_MUTE,
                        serviceData = TelevisionDesiredServiceData(
                            isVolumeMuted = isMuted
                        )
                    )
                } ?: desiredState.volumeLevel?.let { volumeLevel ->
                    DefaultResolvedServiceCommand(
                        service = MediaPlayerService.VOLUME_SET,
                        serviceData = TelevisionDesiredServiceData(
                            volumeLevel = volumeLevel
                        )
                    )
                } ?: desiredState.source?.let { source ->
                    DefaultResolvedServiceCommand(
                        service = MediaPlayerService.VOLUME_SET,
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

val Television.isOn
    get() = actualState.value == SwitchableValue.ON

val Television.isOff
    get() = actualState.value == SwitchableValue.OFF

val Television.isMuted
    get() = actualState.isVolumeMuted == true

fun Television.turnOn() {
    desiredState = TelevisionState(value = SwitchableValue.ON)
}

fun Television.turnOff() {
    desiredState = TelevisionState(value = SwitchableValue.OFF)
}

fun Television.setVolumeTo(level: Double) {
    desiredState = TelevisionState(value = SwitchableValue.ON, volumeLevel = level)
}

fun Television.muteVolume() {
    desiredState = TelevisionState(value = SwitchableValue.ON, isVolumeMuted = true)
}

fun Television.unMuteVolume() {
    desiredState = TelevisionState(value = SwitchableValue.ON, isVolumeMuted = false)
}

fun Television.setSource(source: String) {
    desiredState = TelevisionState(value = SwitchableValue.ON, source = source)
}

fun Television.onTurnedOn(f: Television.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (stateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON))
            f(this, observer)
    }

fun Television.onTurnedOnAsync(f: suspend Television.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, coroutineScope ->
        if (stateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON))
            f(this, observer, coroutineScope)
    }

fun Television.onTurnedOff(f: Television.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (stateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF))
            f(this, observer)
    }

fun Television.onTurnedOffAsync(f: suspend Television.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, coroutineScope ->
        if (stateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF))
            f(this, observer, coroutineScope)
    }
