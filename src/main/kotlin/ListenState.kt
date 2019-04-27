package khome

import khome.Khome.Companion.stateChangeEvents
import khome.Khome.Companion.states
import java.util.*


fun getState(entityId: String) = states[entityId]
fun getAttributes(entityId: String) = states[entityId]?.attributes


fun listenState(entityId: String,callback: ListenState.() -> Unit) {
    val handle = UUID.randomUUID().toString()
    stateChangeEvents[handle] = {
        if (it.event.data.entityId == entityId) {
            val stateListener = ListenState(
                entityId = entityId,
                executeOnce = false,
                handle = handle,
                data = it,
                constraint = true
            )
            stateListener.apply(callback)
        }
    }
}

fun listenStateOnce(entityId: String, callback: ListenState.() -> Unit) {
    val handle = UUID.randomUUID().toString()
    stateChangeEvents[handle] = {
        if (it.event.data.entityId == entityId) {
            val stateListener = ListenState(
                entityId = entityId,
                executeOnce = true,
                handle = handle,
                data = it,
                constraint = true
            )
            stateListener.apply(callback)
        }
    }
}

data class ListenState(
    val entityId: String,
    var executeOnce: Boolean,
    val handle: String,
    val data: EventResult,
    var constraint: Boolean
)

inline fun ListenState.constrain(func: Constraint.() -> Boolean) {
    constraint = func(
        Constraint(
            data.event.data.newState,
            data.event.data.oldState
        )
    )
}

data class Constraint(
    val newState: State,
    val oldState: State
)

fun ListenState.callback(func: EventResult.() -> Unit) {
    if (constraint) func(data)
    if (executeOnce) stateChangeEvents.minusAssign(handle)

}

fun ListenState.unsubscribe() = stateChangeEvents.minusAssign(handle)

