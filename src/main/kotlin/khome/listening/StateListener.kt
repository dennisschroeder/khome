package khome.listening

import khome.core.KhomeComponent
import khome.core.entities.EntityInterface
import khome.core.eventHandling.StateChangeEvent
import kotlinx.coroutines.CoroutineScope
import org.koin.core.get
import java.util.UUID

inline fun <reified Entity : EntityInterface> KhomeComponent.onStateChange(
    crossinline callback: suspend CoroutineScope.(Entity, LifeCycleHandler) -> Unit
) =
    registerStateChangeEvent(callback)

inline fun <reified Entity : EntityInterface> KhomeComponent.registerStateChangeEvent(
    crossinline callback: suspend CoroutineScope.(Entity, LifeCycleHandler) -> Unit
): LifeCycleHandler {
    val handle = UUID.randomUUID().toString()
    val entity = get<Entity>()

    val lifeCycleHandler = LifeCycleHandler(handle)
    val stateChangeEvent: StateChangeEvent = get()

    stateChangeEvent.subscribe(handle) { eventResult ->
        if (eventResult.event.data.entityId == entity.id) {
            callback(entity, lifeCycleHandler)
        }
    }
    return lifeCycleHandler
}
