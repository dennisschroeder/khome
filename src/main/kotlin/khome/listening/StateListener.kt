package khome.listening

import khome.core.KhomeComponent
import khome.core.entities.EntityInterface
import khome.core.eventHandling.StateChangeEvent
import org.koin.core.get
import java.util.UUID

inline fun <reified Entity : EntityInterface> KhomeComponent.onStateChange(
    valueOnly: Boolean = false,
    crossinline callback: suspend Entity.(LifeCycleHandler) -> Unit
) =
    registerStateChangeEvent(valueOnly, callback)

inline fun <reified Entity : EntityInterface> KhomeComponent.registerStateChangeEvent(
    valueOnly: Boolean,
    crossinline callback: suspend Entity.(LifeCycleHandler) -> Unit
): LifeCycleHandler {
    val handle = UUID.randomUUID().toString()
    val entity = get<Entity>()

    val lifeCycleHandler = LifeCycleHandler(handle)
    val stateChangeEvent: StateChangeEvent = get()

    stateChangeEvent.subscribe(handle) {
        if (event.data.entityId == entity.id) {
            callback(entity, lifeCycleHandler)
        }
    }
    return lifeCycleHandler
}
