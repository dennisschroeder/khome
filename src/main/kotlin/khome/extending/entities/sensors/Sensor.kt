package khome.extending.entities.sensors

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.EntityId
import khome.entities.State
import khome.entities.devices.Sensor
import khome.extending.entities.Sensor
import khome.extending.entities.SwitchableState
import khome.extending.entities.SwitchableValue
import khome.extending.entities.sensors.binary.motionDetected

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> KhomeApplication.Sensor(objectId: String): Sensor<S, A> =
    Sensor(EntityId("sensor", objectId))

inline fun <reified S : State<*>, reified A : Attributes, SV> Sensor<S, A>.changedFrom(values: Pair<SV, SV>) =
    history[1].state.value == values.first && measurement.value == values.second

val Sensor<SwitchableState, *>.isOn
    get() = measurement.value == SwitchableValue.ON

val Sensor<SwitchableState, *>.isOff
    get() = measurement.value == SwitchableValue.OFF

val Sensor<SwitchableState, Attributes>.turnedOn
    get() = changedFrom(SwitchableValue.OFF to SwitchableValue.ON)

val Sensor<SwitchableState, Attributes>.turnedOff
    get() = changedFrom(SwitchableValue.ON to SwitchableValue.OFF)

inline fun Sensor<SwitchableState, Attributes>.onActivation(crossinline f: Sensor<SwitchableState, Attributes>.() -> Unit) =
    attachObserver {
        if (turnedOn) f(this)
    }

inline fun Sensor<SwitchableState, Attributes>.onDeactivation(crossinline f: Sensor<SwitchableState, Attributes>.() -> Unit) =
    attachObserver {
        if (turnedOn) f(this)
    }
