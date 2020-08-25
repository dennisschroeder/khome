package khome.extending.entities.actuators

import khome.communicating.CommandDataWithEntityId
import khome.communicating.EntityIdOnlyServiceData
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.SwitchableState
import khome.extending.entities.SwitchableValue
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineScope

fun <S : State<*>, A : Attributes> Actuator<S, A>.callService(
    service: Enum<*>,
    parameterBag: CommandDataWithEntityId = EntityIdOnlyServiceData()
) = callService(service.name, parameterBag)

fun <S : State<*>, A : Attributes, SV> Actuator<S, A>.stateValueChangedFrom(values: Pair<SV, SV>) =
    history[1].state.value == values.first && actualState.value == values.second

val Actuator<SwitchableState, *>.isOn
    get() = actualState.value == SwitchableValue.ON

val Actuator<SwitchableState, *>.isOff
    get() = actualState.value == SwitchableValue.OFF

fun <A : Attributes> Actuator<SwitchableState, A>.turnOn() {
    desiredState = SwitchableState(SwitchableValue.ON)
}

fun <A : Attributes> Actuator<SwitchableState, A>.turnOff() {
    desiredState = SwitchableState(SwitchableValue.OFF)
}

inline fun <A : Attributes> Actuator<SwitchableState, A>.onTurningOn(crossinline f: Actuator<SwitchableState, A>.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (stateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON)) f(this, observer)
    }

inline fun <A : Attributes> Actuator<SwitchableState, A>.onTurningOnAsync(crossinline f: suspend Actuator<SwitchableState, A>.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, scope ->
        if (stateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON)) f(this, observer, scope)
    }

inline fun <A : Attributes> Actuator<SwitchableState, A>.onTurningOff(crossinline f: Actuator<SwitchableState, A>.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (stateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF))
            f(this, observer)
    }

inline fun <A : Attributes> Actuator<SwitchableState, A>.onTurningOffAsync(crossinline f: suspend Actuator<SwitchableState, A>.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, scope ->
        if (stateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF))
            f(this, observer, scope)
    }
