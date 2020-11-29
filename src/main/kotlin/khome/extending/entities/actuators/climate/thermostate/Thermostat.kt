package khome.extending.entities.actuators.climate.thermostate

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.actuators.climate.ClimateControl
import khome.extending.entities.actuators.stateValueChangedFrom
import khome.observability.Switchable
import khome.values.ObjectId
import khome.values.Temperature
import khome.values.UserId
import khome.values.service
import java.time.Instant

typealias Thermostat = Actuator<ThermostatState, ThermostatAttributes>

@Suppress("FunctionName")
fun KhomeApplication.Thermostat(objectId: ObjectId): Thermostat {
    return ClimateControl(objectId, ServiceCommandResolver { desiredState ->
        when (desiredState.value) {
            ThermostatStateValue.OFF -> {
                DefaultResolvedServiceCommand(
                    service = "turn_off".service,
                    serviceData = EntityIdOnlyServiceData()
                )
            }

            ThermostatStateValue.HEAT -> {
                desiredState.temperature?.let { temperature ->
                    DefaultResolvedServiceCommand(
                        service = "set_temperature".service,
                        serviceData = ThermostatServiceData(temperature, hvacMode = "heat")
                    )
                } ?: (if (desiredState.presetMode == "none") null else desiredState.presetMode)?.let { preset ->
                    DefaultResolvedServiceCommand(
                        service = "set_preset_mode".service,
                        serviceData = ThermostatServiceData(presetMode = preset)
                    )
                } ?: DefaultResolvedServiceCommand(
                    service = "turn_on".service,
                    serviceData = EntityIdOnlyServiceData()
                )
            }
        }
    })
}

data class ThermostatState(
    override val value: ThermostatStateValue,
    val temperature: Temperature? = null,
    val presetMode: String = "none"
) : State<ThermostatStateValue>

data class ThermostatAttributes(
    val hvacModes: List<String>,
    val presetModes: List<String>,
    val currentTemperature: Temperature,
    val minTemp: Temperature,
    val maxTemp: Temperature,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant,
    override val userId: UserId?
) : Attributes

enum class ThermostatStateValue {
    @SerializedName("heat")
    HEAT,

    @SerializedName("off")
    OFF
}

data class ThermostatServiceData(
    val temperature: Temperature? = null,
    val presetMode: String? = null,
    val hvacMode: String? = null
) : DesiredServiceData()

val Thermostat.isHeating
    get() = actualState.value == ThermostatStateValue.HEAT

val Thermostat.isOn
    get() = isHeating

val Thermostat.isOff
    get() = actualState == ThermostatState(ThermostatStateValue.OFF)

fun Thermostat.turnOff() {
    desiredState = ThermostatState(ThermostatStateValue.OFF)
}

fun Thermostat.turnOn() {
    desiredState = ThermostatState(ThermostatStateValue.HEAT)
}

fun Thermostat.setPreset(preset: String) {
    desiredState = ThermostatState(ThermostatStateValue.HEAT, presetMode = preset)
}

fun Thermostat.setTargetTemperature(temperature: Temperature) {
    desiredState = ThermostatState(ThermostatStateValue.HEAT, temperature = temperature)
}

fun Thermostat.turnOnBoost() = setPreset("boost")

fun Thermostat.onTurnedOn(f: Thermostat.(Switchable) -> Unit) =
    attachObserver {
        if (stateValueChangedFrom(ThermostatStateValue.OFF to ThermostatStateValue.HEAT))
            f(this, it)
    }

fun Thermostat.onTurnedOff(f: Thermostat.(Switchable) -> Unit) =
    attachObserver {
        if (stateValueChangedFrom(ThermostatStateValue.HEAT to ThermostatStateValue.OFF))
            f(this, it)
    }
