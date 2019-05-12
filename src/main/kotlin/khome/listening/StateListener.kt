package khome.listening

import khome.core.*
import java.util.*
import khome.Khome.Companion.states
import khome.scheduling.runOnceInSeconds
import khome.scheduling.runOnceInMinutes
import khome.core.entities.EntityInterface
import khome.Khome.Companion.stateChangeEvents
import khome.listening.exceptions.EntityStateNotFoundException
import kotlin.reflect.KClass

fun getState(entityId: String) =
    states[entityId] ?: throw EntityStateNotFoundException("No state for entity with id: $entityId found.")

inline fun <reified StateValueType> getStateValue(entityId: String) = getState(entityId).getValue<StateValueType>()!!
inline fun <reified StateValueType> getStateValue(entity: EntityInterface) =
    entity.state.value.getValue<StateValueType>()

fun getStateAttributes(entityId: String) = getState(entityId).attributes
fun getStateAttributes(entity: EntityInterface) = entity.attributes

inline fun listenState(entityId: String, crossinline callback: StateListener.() -> Unit) =
    registerStateChangeEvent(entityId, callback)

inline fun <reified Entity: EntityInterface> listenState(crossinline callback: StateListener.() -> Unit) {
    val entity = getEntityInstance<Entity>()
    val entityId = entity.entityId

    registerStateChangeEvent(entityId, callback)
}

inline fun <reified Entity: EntityInterface> getEntityInstance() = getEntityInstance(Entity::class)

inline fun <reified Entity: EntityInterface> getEntityInstance(type: KClass<Entity>): Entity {
    return type.objectInstance as Entity
}

inline fun registerStateChangeEvent(
    entityId: String,
    crossinline callback: StateListener.() -> Unit
): LifeCycleHandler {
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