package khome.extending.entities.actuators.light

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.communicating.ServiceType
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.SwitchableValue
import khome.extending.entities.actuators.callService
import khome.extending.entities.actuators.stateValueChangedFrom
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineScope

typealias ColorableLight = Actuator<ColorableLightState, LightAttributes>

@Suppress("FunctionName")
fun KhomeApplication.ColorableLight(objectId: String): ColorableLight =
    Light(objectId, ServiceCommandResolver { desiredState ->
        when (desiredState.value) {
            SwitchableValue.OFF -> {
                DefaultResolvedServiceCommand(
                    service = ServiceType.TURN_OFF,
                    serviceData = EntityIdOnlyServiceData()
                )
            }

            SwitchableValue.ON -> {
                desiredState.colorTemp?.let {
                    DefaultResolvedServiceCommand(
                        service = ServiceType.TURN_ON,
                        serviceData = ColorableLightServiceData(
                            colorTemp = it
                        )
                    )
                } ?: desiredState.hsColor?.let {
                    DefaultResolvedServiceCommand(
                        service = ServiceType.TURN_ON,
                        serviceData = ColorableLightServiceData(
                            hsColor = it
                        )
                    )
                } ?: desiredState.rgbColor?.let {
                    DefaultResolvedServiceCommand(
                        service = ServiceType.TURN_ON,
                        serviceData = ColorableLightServiceData(
                            rgbColor = it
                        )
                    )
                } ?: desiredState.brightness?.let {
                    DefaultResolvedServiceCommand(
                        service = ServiceType.TURN_ON,
                        serviceData = ColorableLightServiceData(
                            brightness = it
                        )
                    )
                } ?: desiredState.xyColor?.let {
                    DefaultResolvedServiceCommand(
                        service = ServiceType.TURN_ON,
                        serviceData = ColorableLightServiceData(
                            xyColor = it
                        )
                    )
                }

                ?: DefaultResolvedServiceCommand(
                    service = ServiceType.TURN_ON,
                    serviceData = EntityIdOnlyServiceData()
                )
            }

            SwitchableValue.UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
        }
    })

data class ColorableLightServiceData(
    private val brightness: Int? = null,
    private val hsColor: List<Double>? = null,
    private val rgbColor: List<Int>? = null,
    private val xyColor: List<Double>? = null,
    private val colorTemp: Int? = null
) : DesiredServiceData()

data class ColorableLightState(
    override val value: SwitchableValue,
    val brightness: Int? = null,
    val hsColor: List<Double>? = null,
    val rgbColor: List<Int>? = null,
    val xyColor: List<Double>? = null,
    val colorTemp: Int? = null
) : State<SwitchableValue>

val ColorableLight.isOn
    get() = actualState.value == SwitchableValue.ON

val ColorableLight.isOff
    get() = actualState.value == SwitchableValue.OFF

fun ColorableLight.turnOn() {
    desiredState = ColorableLightState(SwitchableValue.ON)
}

fun ColorableLight.turnOff() {
    desiredState = ColorableLightState(SwitchableValue.OFF)
}

fun ColorableLight.setBrightness(level: Int) {
    desiredState = ColorableLightState(SwitchableValue.ON, level)
}

fun ColorableLight.setRGB(red: Int, green: Int, blue: Int) {
    desiredState = ColorableLightState(SwitchableValue.ON, rgbColor = listOf(red, green, blue))
}

fun ColorableLight.setHS(hue: Double, saturation: Double) {
    desiredState = ColorableLightState(SwitchableValue.ON, hsColor = listOf(hue, saturation))
}

fun ColorableLight.setXY(x: Double, y: Double) {
    desiredState = ColorableLightState(SwitchableValue.ON, xyColor = listOf(x, y))
}

data class NamedColorServiceData(val color_name: String) : DesiredServiceData()

fun ColorableLight.setColor(name: String) {
    callService(ServiceType.TURN_ON, NamedColorServiceData(name))
}

fun ColorableLight.onTurningOn(f: ColorableLight.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (stateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON))
            f(this, observer)
    }

fun ColorableLight.onTurningOnAsync(f: suspend ColorableLight.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, scope ->
        if (stateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON))
            f(this, observer, scope)
    }

fun ColorableLight.onTurningOff(f: ColorableLight.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (stateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF))
            f(this, observer)
    }

fun ColorableLight.onTurningOffAsync(f: suspend ColorableLight.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, scope ->
        if (stateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF))
            f(this, observer, scope)
    }
