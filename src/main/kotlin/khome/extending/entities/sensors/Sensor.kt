package khome.extending.entities.sensors

import khome.KhomeApplication
import khome.entities.Attributes
import khome.values.EntityId
import khome.entities.State
import khome.entities.devices.Sensor
import khome.extending.entities.Sensor
import khome.extending.entities.SwitchableState
import khome.extending.entities.SwitchableValue
import khome.values.ObjectId
import khome.values.domain

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> KhomeApplication.Sensor(objectId: ObjectId): Sensor<S, A> =
    Sensor(EntityId.fromPair("sensor".domain to objectId))

fun <S : State<*>, A : Attributes, SV> Sensor<S, A>.measurementValueChangedFrom(values: Pair<SV, SV>) =
    history[1].state.value == values.first && measurement.value == values.second

val Sensor<SwitchableState, *>.isOn
    get() = measurement.value == SwitchableValue.ON

val Sensor<SwitchableState, *>.isOff
    get() = measurement.value == SwitchableValue.OFF

inline fun <A : Attributes> Sensor<SwitchableState, A>.onTurnedOn(
    crossinline f: Sensor<SwitchableState, A>.() -> Unit
) =
    attachObserver {
        if (measurementValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON))
            f(this)
    }

inline fun <A : Attributes> Sensor<SwitchableState, A>.onTurnedOff(
    crossinline f: Sensor<SwitchableState, A>.() -> Unit
) =
    attachObserver {
        if (measurementValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF))
            f(this)
    }
