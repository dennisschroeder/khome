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

fun <S : State<*>, A : Attributes, SV> Sensor<S, A>.measurementValueChangedFrom(values: Pair<SV, SV>) =
    history[1].state.value == values.first && measurement.value == values.second

val Sensor<SwitchableState, *>.isOn
    get() = measurement.value == SwitchableValue.ON

val Sensor<SwitchableState, *>.isOff
    get() = measurement.value == SwitchableValue.OFF

inline fun <A : Attributes> Sensor<SwitchableState, A>.onTurnedOn(crossinline f: Sensor<SwitchableState, A>.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (measurementValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON))
            f(this, observer)
    }

inline fun <A : Attributes> Sensor<SwitchableState, A>.onTurnedOnAsync(crossinline f: suspend Sensor<SwitchableState, A>.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, coroutineScope ->
        if (measurementValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON))
            f(this, observer, coroutineScope)
    }

inline fun <A : Attributes> Sensor<SwitchableState, A>.onTurnedOff(crossinline f: Sensor<SwitchableState, A>.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (measurementValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF))
            f(this, observer)
    }

inline fun <A : Attributes> Sensor<SwitchableState, A>.onTurnedOffAsync(crossinline f: suspend Sensor<SwitchableState, A>.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, coroutineScope ->
        if (measurementValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF))
            f(this, observer, coroutineScope)
    }
