package khome.listening

import khome.KhomeSession
import khome.core.entities.EntityInterface
import khome.core.eventHandling.StateChangeEvents
import org.koin.core.get
import org.koin.core.inject
import java.util.UUID

inline fun <reified Entity : EntityInterface> KhomeSession.onStateChange(crossinline callback: Entity.() -> Unit) =
    registerStateChangeEvent(callback)

inline fun <reified Entity : EntityInterface> KhomeSession.registerStateChangeEvent(
    crossinline callback: Entity.() -> Unit
): LifeCycleHandler {
    val handle = UUID.randomUUID().toString()
    val entity = get<Entity>()

    val lifeCycleHandler = LifeCycleHandler(handle, entity)
    val stateChangeEvents: StateChangeEvents by inject()

    stateChangeEvents.subscribe(handle) {
        if (event.data.entityId == entity.id) {
            callback(entity)
        }
    }
    return lifeCycleHandler
}

inline fun <reified StateValueType> getStateValueFromEntity(entity: EntityInterface): StateValueType =
    entity.state.getValue()

inline fun <reified AttributeValueType> getStateAttributesFromEntity(
    entity: EntityInterface,
    key: String
): AttributeValueType = entity.state.getAttribute(key)
