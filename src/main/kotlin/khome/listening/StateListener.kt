package khome.listening

import khome.Khome.Companion.isSandBoxModeActive
import java.util.*
import khome.core.*
import kotlin.reflect.KClass
import khome.Khome.Companion.states
import khome.core.entities.EntityInterface
import khome.Khome.Companion.stateChangeEvents
import khome.listening.exceptions.EntityStateNotFoundException


inline fun <reified Entity : EntityInterface> listenState(crossinline callback: StateListener.() -> Unit): LifeCycleHandler {
    val entity = getEntityInstance<Entity>()

    return registerStateChangeEvent(entity, callback)
}

inline fun <reified Entity : EntityInterface> getEntityInstance() = getEntityInstance(Entity::class)

inline fun <reified Entity : EntityInterface> getEntityInstance(type: KClass<Entity>): Entity {
    return type.objectInstance as Entity
}

inline fun registerStateChangeEvent(
    entity: EntityInterface,
    crossinline callback: StateListener.() -> Unit
): LifeCycleHandler {
    val handle = UUID.randomUUID().toString()
    val lifeCycleHandler = LifeCycleHandler(handle, entity)

    stateChangeEvents[handle] = {
        if (it.event.data.entityId == entity.id) {
            val stateListener = StateListener(
                entityId = entity.id,
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
    private var constraint: Boolean,
    val lifeCycleHandler: LifeCycleHandler
) {
    inline fun runInTesting(action: () -> Unit) {
        if (isSandBoxModeActive()) action()
    }

    inline fun excludeFromTesting(action: () -> Unit) {
        if (!isSandBoxModeActive()) action()
    }

    fun constrain(func: Constraint.() -> Boolean) {
        constraint = func(
            Constraint(
                data.event.data.newState,
                data.event.data.oldState
            )
        )
    }

    fun execute(func: EventResult.() -> Unit) {
        if (constraint) func(data)
    }

    fun disable() = lifeCycleHandler.disable()
}

fun getState(entity: EntityInterface): State  {
    return states[entity.id] ?: throw EntityStateNotFoundException("Could not fetch state object for entity: ${entity.id}")
}

inline fun <reified Entity : EntityInterface,  reified StateValueType> getStateValue(): StateValueType {
    val entity = getEntityInstance<Entity>()
    return entity.state.getValue()
}

inline fun <reified Entity : EntityInterface, reified AttributeValueType> getStateAttributes(key: String): AttributeValueType {
    val entity = getEntityInstance<Entity>()
    return entity.state.getAttribute(key)
}