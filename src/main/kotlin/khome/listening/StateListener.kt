package khome.listening

import java.util.*
import khome.core.*
import kotlin.reflect.KClass
import khome.Khome.Companion.states
import khome.core.entities.EntityInterface
import khome.Khome.Companion.isSandBoxModeActive
import khome.Khome.Companion.subscribeStateChangeEvent
import khome.listening.exceptions.EntityStateNotFoundException

fun listenState(entity: EntityInterface, callback: StateListener.() -> Unit): LifeCycleHandler =
    registerStateChangeEvent(entity, callback)

inline fun <reified Entity : EntityInterface> getEntityInstance() = getEntityInstance(Entity::class)

inline fun <reified Entity : EntityInterface> getEntityInstance(type: KClass<Entity>): Entity {
    return type.objectInstance as Entity
}

internal inline fun registerStateChangeEvent(
    entity: EntityInterface,
    crossinline callback: StateListener.() -> Unit
): LifeCycleHandler {
    val handle = UUID.randomUUID().toString()
    val lifeCycleHandler = LifeCycleHandler(handle, entity)

    subscribeStateChangeEvent(handle) {
        if (event.data.entityId == entity.id) {
            val stateListener = StateListener(
                entityId = entity.id,
                handle = handle,
                data = this,
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
    fun runInTesting(action: () -> Unit) {
        if (isSandBoxModeActive)
            action()
    }

    fun excludeFromTesting(action: () -> Unit) {
        if (!isSandBoxModeActive)
            action()
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
        if (constraint && !isSandBoxModeActive) func(data)
        if (isSandBoxModeActive) func(data)
    }

    fun disable() = lifeCycleHandler.disable()
}

fun getState(entity: EntityInterface): State {
    return states[entity.id]
        ?: throw EntityStateNotFoundException("Could not fetch state object for entity: ${entity.id}")
}

inline fun <reified StateValueType> getStateValueFromEntity(entity: EntityInterface): StateValueType =
    entity.state.getValue()

inline fun <reified AttributeValueType> getStateAttributesFromEntity(
    entity: EntityInterface,
    key: String
): AttributeValueType = entity.state.getAttribute(key)