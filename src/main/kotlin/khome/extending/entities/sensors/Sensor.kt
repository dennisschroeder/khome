package khome.extending.entities.sensors

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.EntityId
import khome.entities.State
import khome.entities.devices.Sensor
import khome.extending.entities.Sensor
import khome.extending.entities.SwitchableState
import khome.extending.entities.SwitchableValue
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineScope

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> KhomeApplication.Sensor(objectId: String): Sensor<S, A> =
    Sensor(EntityId("sensor", objectId))

inline fun <reified S : State<*>, reified A : Attributes, SV> Sensor<S, A>.measurementValueChangedFrom(values: Pair<SV, SV>) =
    history[1].state.value == values.first && measurement.value == values.second

val Sensor<SwitchableState, *>.isOn
    get() = measurement.value == SwitchableValue.ON

val Sensor<SwitchableState, *>.isOff
    get() = measurement.value == SwitchableValue.OFF

inline fun Sensor<SwitchableState, Attributes>.onActivation(crossinline f: Sensor<SwitchableState, Attributes>.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (measurementValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON))
            f(this, observer)
    }

inline fun Sensor<SwitchableState, Attributes>.onActivationAsync(crossinline f: suspend Sensor<SwitchableState, Attributes>.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, coroutineScope ->
        if (measurementValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON))
            f(this, observer, coroutineScope)
    }

inline fun Sensor<SwitchableState, Attributes>.onDeactivation(crossinline f: Sensor<SwitchableState, Attributes>.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (measurementValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF))
            f(this, observer)
    }

inline fun Sensor<SwitchableState, Attributes>.onDeactivationAsync(crossinline f: suspend Sensor<SwitchableState, Attributes>.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, coroutineScope ->
        if (measurementValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF))
            f(this, observer, coroutineScope)
    }
