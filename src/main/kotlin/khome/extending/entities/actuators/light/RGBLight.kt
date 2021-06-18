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
import khome.values.HSColor
import khome.values.ObjectId
import khome.values.RGBColor
import khome.values.XYColor
import khome.values.service

typealias RGBLight = Actuator<RGBLightState, LightAttributes>

@Suppress("FunctionName")
fun KhomeApplication.RGBLight(objectId: ObjectId): RGBLight =
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
                    desiredState.hsColor?.let {
                        DefaultResolvedServiceCommand(
                            service = "turn_on".service,
                            serviceData = RGBLightServiceData(
                                hsColor = it
                            )
                        )
                    } ?: desiredState.rgbColor?.let {
                        DefaultResolvedServiceCommand(
                            service = "turn_on".service,
                            serviceData = RGBLightServiceData(
                                rgbColor = it
                            )
                        )
                    } ?: desiredState.brightness?.let {
                        DefaultResolvedServiceCommand(
                            service = "turn_on".service,
                            serviceData = RGBLightServiceData(
                                brightness = it
                            )
                        )
                    } ?: desiredState.xyColor?.let {
                        DefaultResolvedServiceCommand(
                            service = "turn_on".service,
                            serviceData = RGBLightServiceData(
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

data class RGBLightServiceData(
    private val brightness: Brightness? = null,
    private val hsColor: HSColor? = null,
    private val rgbColor: RGBColor? = null,
    private val xyColor: XYColor? = null
) : DesiredServiceData()

data class RGBLightState(
    override val value: SwitchableValue,
    val brightness: Brightness? = null,
    val hsColor: HSColor? = null,
    val rgbColor: RGBColor? = null,
    val xyColor: XYColor? = null
) : State<SwitchableValue>

val RGBLight.isOn
    get() = actualState.value == SwitchableValue.ON

val RGBLight.isOff
    get() = actualState.value == SwitchableValue.OFF

fun RGBLight.turnOn() {
    desiredState = RGBLightState(SwitchableValue.ON)
}

fun RGBLight.turnOff() {
    desiredState = RGBLightState(SwitchableValue.OFF)
}

fun RGBLight.setBrightness(level: Brightness) {
    desiredState = RGBLightState(SwitchableValue.ON, level)
}

fun RGBLight.setRGB(red: Int, green: Int, blue: Int) {
    desiredState = RGBLightState(SwitchableValue.ON, rgbColor = RGBColor.from(red, green, blue))
}

fun RGBLight.setHS(hue: Double, saturation: Double) {
    desiredState = RGBLightState(SwitchableValue.ON, hsColor = HSColor.from(hue, saturation))
}

fun RGBLight.setXY(x: Double, y: Double) {
    desiredState = RGBLightState(SwitchableValue.ON, xyColor = XYColor.from(x, y))
}

fun RGBLight.setColor(name: ColorName) =
    callService("turn_on".service, NamedColorServiceData(name))

fun RGBLight.onTurnedOn(f: RGBLight.(Switchable) -> Unit) =
    onStateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON, f)

fun RGBLight.onTurnedOff(f: RGBLight.(Switchable) -> Unit) =
    onStateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF, f)
