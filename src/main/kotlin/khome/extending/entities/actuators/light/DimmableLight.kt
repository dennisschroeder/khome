package khome.extending.entities.actuators.light

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ResolvedServiceCommand
import khome.communicating.ServiceCommandResolver
import khome.communicating.ServiceType
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.SwitchableValue
import khome.extending.entities.actuators.stateValueChangedFrom
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineScope

typealias DimmableLight = Actuator<DimmableLightState, LightAttributes>

@Suppress("FunctionName")
fun KhomeApplication.DimmableLight(objectId: String): DimmableLight =
    Light(objectId, ServiceCommandResolver { desiredState ->
        when (desiredState.value) {
            SwitchableValue.OFF -> {
                val resolvedServiceCommand: ResolvedServiceCommand = desiredState.brightness?.let { brightness ->
                    DefaultResolvedServiceCommand(
                        service = ServiceType.TURN_ON,
                        serviceData = DimmableLightServiceData(
                            brightness
                        )
                    )
                } ?: DefaultResolvedServiceCommand(
                    service = ServiceType.TURN_OFF,
                    serviceData = EntityIdOnlyServiceData()
                )
                resolvedServiceCommand
            }
            SwitchableValue.ON -> {
                desiredState.brightness?.let { brightness ->
                    DefaultResolvedServiceCommand(
                        service = ServiceType.TURN_ON,
                        serviceData = DimmableLightServiceData(
                            brightness
                        )
                    )
                } ?: DefaultResolvedServiceCommand(
                    service = ServiceType.TURN_ON,
                    serviceData = EntityIdOnlyServiceData()
                )
            }
        }
    })

data class DimmableLightState(override val value: SwitchableValue, val brightness: Int? = null) : State<SwitchableValue>

data class DimmableLightServiceData(private val brightness: Int) : DesiredServiceData()

val DimmableLight.isOn
    get() = actualState.value == SwitchableValue.ON

val DimmableLight.isOff
    get() = actualState.value == SwitchableValue.OFF

fun DimmableLight.turnOn() {
    desiredState = DimmableLightState(SwitchableValue.ON)
}

fun DimmableLight.turnOff() {
    desiredState = DimmableLightState(SwitchableValue.OFF)
}

fun DimmableLight.setBrightness(level: Int) {
    desiredState = DimmableLightState(SwitchableValue.ON, level)
}

fun DimmableLight.onTurningOn(f: DimmableLight.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (stateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON))
            f(this, observer)
    }

fun DimmableLight.onTurningOnAsync(f: suspend DimmableLight.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, scope ->
        if (stateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON))
            f(this, observer, scope)
    }

fun DimmableLight.onTurningOff(f: DimmableLight.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (stateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF))
            f(this, observer)
    }

fun DimmableLight.onTurningOffAsync(f: suspend DimmableLight.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, scope ->
        if (stateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF))
            f(this, observer, scope)
    }
