package khome.extending.entities.actuators.light

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.SwitchableValue
import khome.extending.entities.actuators.stateValueChangedFrom
import khome.observability.Switchable
import khome.values.ObjectId
import khome.values.service

typealias RGBWLight = Actuator<RGBWLightState, LightAttributes>

@Suppress("FunctionName")
fun KhomeApplication.RGBWLight(objectId: ObjectId): RGBWLight =
    Light(objectId, ServiceCommandResolver { desiredState ->
        when (desiredState.value) {
            SwitchableValue.OFF -> {
                DefaultResolvedServiceCommand(
                    service = "turn_off".service,
                    serviceData = EntityIdOnlyServiceData()
                )
            }

            SwitchableValue.ON -> {
                desiredState.colorTemp?.let {
                    DefaultResolvedServiceCommand(
                        service = "turn_on".service,
                        serviceData = RGBWLightServiceData(
                            colorTemp = it
                        )
                    )
                } ?: desiredState.hsColor?.let {
                    DefaultResolvedServiceCommand(
                        service = "turn_on".service,
                        serviceData = RGBWLightServiceData(
                            hsColor = it
                        )
                    )
                } ?: desiredState.rgbColor?.let {
                    DefaultResolvedServiceCommand(
                        service = "turn_on".service,
                        serviceData = RGBWLightServiceData(
                            rgbColor = it
                        )
                    )
                } ?: desiredState.brightness?.let {
                    DefaultResolvedServiceCommand(
                        service = "turn_on".service,
                        serviceData = RGBWLightServiceData(
                            brightness = it
                        )
                    )
                } ?: desiredState.xyColor?.let {
                    DefaultResolvedServiceCommand(
                        service = "turn_on".service,
                        serviceData = RGBWLightServiceData(
                            xyColor = it
                        )
                    )
                }

                ?: DefaultResolvedServiceCommand(
                    service = "turn_on".service,
                    serviceData = EntityIdOnlyServiceData()
                )
            }

            SwitchableValue.UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
        }
    })

data class RGBWLightServiceData(
    private val brightness: Int? = null,
    private val hsColor: List<Double>? = null,
    private val rgbColor: List<Int>? = null,
    private val xyColor: List<Double>? = null,
    private val colorTemp: Int? = null
) : DesiredServiceData()

data class RGBWLightState(
    override val value: SwitchableValue,
    val brightness: Int? = null,
    val hsColor: List<Double>? = null,
    val rgbColor: List<Int>? = null,
    val xyColor: List<Double>? = null,
    val colorTemp: Int? = null
) : State<SwitchableValue>

val RGBWLight.isOn
    get() = actualState.value == SwitchableValue.ON

val RGBWLight.isOff
    get() = actualState.value == SwitchableValue.OFF

fun RGBWLight.turnOn() {
    desiredState = RGBWLightState(SwitchableValue.ON)
}

fun RGBWLight.turnOff() {
    desiredState = RGBWLightState(SwitchableValue.OFF)
}

fun RGBWLight.setBrightness(level: Int) {
    desiredState = RGBWLightState(SwitchableValue.ON, level)
}

fun RGBWLight.setRGB(red: Int, green: Int, blue: Int) {
    desiredState = RGBWLightState(SwitchableValue.ON, rgbColor = listOf(red, green, blue))
}

fun RGBWLight.setHS(hue: Double, saturation: Double) {
    desiredState = RGBWLightState(SwitchableValue.ON, hsColor = listOf(hue, saturation))
}

fun RGBWLight.setXY(x: Double, y: Double) {
    desiredState = RGBWLightState(SwitchableValue.ON, xyColor = listOf(x, y))
}

fun RGBWLight.setColorTemperature(temperature: Int) {
    desiredState = RGBWLightState(SwitchableValue.ON, colorTemp = temperature)
}

fun RGBWLight.setColor(name: String) {
    callService("turn_on".service, NamedColorServiceData(name))
}

fun RGBWLight.onTurnedOn(f: RGBWLight.(Switchable) -> Unit) =
    attachObserver {
        if (stateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON))
            f(this, it)
    }

fun RGBWLight.onTurnedOff(f: RGBWLight.(Switchable) -> Unit) =
    attachObserver {
        if (stateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF))
            f(this, it)
    }
