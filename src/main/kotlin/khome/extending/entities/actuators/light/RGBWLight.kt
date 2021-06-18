package khome.extending.entities.actuators.light

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.SwitchableValue
import khome.extending.entities.actuators.onStateValueChangedFrom
import khome.observability.Switchable
import khome.values.Brightness
import khome.values.ColorName
import khome.values.ColorTemperature
import khome.values.HSColor
import khome.values.ObjectId
import khome.values.RGBColor
import khome.values.XYColor
import khome.values.service

typealias RGBWLight = Actuator<RGBWLightState, LightAttributes>

@Suppress("FunctionName")
fun KhomeApplication.RGBWLight(objectId: ObjectId): RGBWLight =
    Light(
        objectId,
        ServiceCommandResolver { desiredState ->
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
                    } ?: DefaultResolvedServiceCommand(
                        service = "turn_on".service,
                        serviceData = EntityIdOnlyServiceData()
                    )
                }

                SwitchableValue.UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
            }
        }
    )

data class RGBWLightServiceData(
    private val brightness: Brightness? = null,
    private val hsColor: HSColor? = null,
    private val rgbColor: RGBColor? = null,
    private val xyColor: XYColor? = null,
    private val colorTemp: ColorTemperature? = null
) : DesiredServiceData()

data class RGBWLightState(
    override val value: SwitchableValue,
    val brightness: Brightness? = null,
    val hsColor: HSColor? = null,
    val rgbColor: RGBColor? = null,
    val xyColor: XYColor? = null,
    val colorTemp: ColorTemperature? = null
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

fun RGBWLight.setBrightness(level: Brightness) {
    desiredState = RGBWLightState(SwitchableValue.ON, level)
}

fun RGBWLight.setRGB(red: Int, green: Int, blue: Int) {
    desiredState = RGBWLightState(SwitchableValue.ON, rgbColor = RGBColor.from(red, green, blue))
}

fun RGBWLight.setHS(hue: Double, saturation: Double) {
    desiredState = RGBWLightState(SwitchableValue.ON, hsColor = HSColor.from(hue, saturation))
}

fun RGBWLight.setXY(x: Double, y: Double) {
    desiredState = RGBWLightState(SwitchableValue.ON, xyColor = XYColor.from(x, y))
}

fun RGBWLight.setColorTemperature(temperature: ColorTemperature) {
    when (temperature.unit) {
        ColorTemperature.Unit.MIRED -> desiredState = RGBWLightState(SwitchableValue.ON, colorTemp = temperature)
        ColorTemperature.Unit.KELVIN -> callService("turn_on".service, KelvinServiceData(temperature))
    }
}

fun RGBWLight.setColor(name: ColorName) {
    callService("turn_on".service, NamedColorServiceData(name))
}

fun RGBWLight.onTurnedOn(f: RGBWLight.(Switchable) -> Unit) =
    onStateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON, f)

fun RGBWLight.onTurnedOff(f: RGBWLight.(Switchable) -> Unit) =
    onStateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF, f)
