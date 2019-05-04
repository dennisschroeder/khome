package khome.listening

import java.util.*
import khome.core.State
import khome.core.logger
import khome.core.EventResult
import khome.Khome.Companion.states
import khome.scheduling.runOnceInSeconds
import khome.scheduling.runOnceInMinutes
import khome.core.LifeCycleHandlerInterface
import khome.Khome.Companion.stateChangeEvents

fun getState(entityId: String) = states[entityId]
fun getStateAttributes(entityId: String) = states[entityId]?.attributes

inline fun listenState(entityId: String, crossinline callback: StateListener.() -> Unit) = registerStateChangeEvent(entityId, callback)

inline fun registerStateChangeEvent(entityId: String, crossinline callback: StateListener.() -> Unit): LifeCycleHandler {
    val handle = UUID.randomUUID().toString()
    val lifeCycleHandler = LifeCycleHandler(handle, entityId)

    stateChangeEvents[handle] = {
        if (it.event.data.entityId == entityId) {
            val stateListener = StateListener(
                entityId = entityId,
                handle = handle,
                data = it,
                constraint = true,
                lifeCycleHandler = lifeCycleHandler
            )
            stateListener.apply(callback)
        }
    }
    return lifeCycleHandler
}

data class StateListener(
    val entityId: String,
    val handle: String,
    val data: EventResult,
    var constraint: Boolean,
    val lifeCycleHandler: LifeCycleHandler
)

inline fun StateListener.constrain(func: Constraint.() -> Boolean) {
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

fun StateListener.action(func: EventResult.() -> Unit) {
    if (constraint) func(data)
}

fun StateListener.cancel() {
    lifeCycleHandler.cancel()
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