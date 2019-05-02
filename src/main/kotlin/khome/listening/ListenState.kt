package khome.listening

import khome.EventResult
import khome.Khome.Companion.logger
import khome.Khome.Companion.stateChangeEvents
import khome.Khome.Companion.states
import khome.State
import khome.core.LifeCycleHandlerInterface
import khome.scheduling.runOnceInMinutes
import khome.scheduling.runOnceInSeconds
import java.util.*


fun getState(entityId: String) = states[entityId]
fun getAttributes(entityId: String) = states[entityId]?.attributes


fun listenState(entityId: String, callback: ListenState.() -> Unit): LifeCycleHandler {
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

    return LifeCycleHandler(handle, entityId)
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

fun ListenState.action(func: EventResult.() -> Unit) {
    if (constraint) func(data)
    if (executeOnce) stateChangeEvents.minusAssign(handle)

}

class LifeCycleHandler(handle: String, entityId: String) : LifeCycleHandlerInterface {
    override val lazyCancellation: Unit by lazy {
        stateChangeEvents.minusAssign(handle)
        logger.info { "Subscription to $entityId canceled." }
    }

    override fun cancel() = lazyCancellation
    override fun cancelInSeconds(seconds: Int) = runOnceInSeconds(seconds) { lazyCancellation }
    override fun cancelInMinutes(minutes: Int) = runOnceInMinutes(minutes) { lazyCancellation }

}