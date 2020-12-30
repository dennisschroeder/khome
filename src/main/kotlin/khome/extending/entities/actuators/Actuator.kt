@file:Suppress("unused")

package khome.extending.entities.actuators

import khome.communicating.CommandDataWithEntityId
import khome.communicating.EntityIdOnlyServiceData
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.SwitchableState
import khome.extending.entities.SwitchableValue
import khome.observability.Switchable
import khome.values.Service

fun <S : State<*>, A : Attributes> Actuator<S, A>.callService(
    service: Service,
    parameterBag: CommandDataWithEntityId = EntityIdOnlyServiceData()
) = callService(service, parameterBag)

fun <S : State<*>, A : Attributes, SV> Actuator<S, A>.stateValueChangedFrom(values: Pair<SV, SV>) =
    history[1].state.value == values.first && actualState.value == values.second

fun <S : State<*>, A : Attributes, SV> Actuator<S, A>.stateValueChangedFrom(values: Triple<SV, SV, SV>) =
    history[2].state.value == values.first && history[1].state.value == values.second && actualState.value == values.third

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

inline fun <S : State<*>, A : Attributes, SV> Actuator<S, A>.onStateValueChangedFrom(
    values: Pair<SV, SV>,
    crossinline f: Actuator<S, A>.(Switchable) -> Unit
) = attachObserver {
    if (stateValueChangedFrom(values))
        f(this, it)
}

inline fun <S : State<*>, A : Attributes, SV> Actuator<S, A>.onStateValueChangedFrom(
    values: Triple<SV, SV, SV>,
    crossinline f: Actuator<S, A>.(Switchable) -> Unit
) = attachObserver {
    if (stateValueChangedFrom(values))
        f(this, it)
}

inline fun <A : Attributes> Actuator<SwitchableState, A>.onTurnedOn(
    crossinline f: Actuator<SwitchableState, A>.(Switchable) -> Unit
) = onStateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON, f)

inline fun <A : Attributes> Actuator<SwitchableState, A>.onTurnedOff(
    crossinline f: Actuator<SwitchableState, A>.(Switchable) -> Unit
) = onStateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF, f)
