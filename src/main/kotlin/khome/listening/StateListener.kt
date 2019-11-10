package khome.listening

import khome.KhomeSession
import khome.core.State
import khome.core.StateInterface
import khome.core.entities.EntityInterface
import khome.core.eventHandling.StateChangeEvents
import org.koin.core.get
import org.koin.core.inject
import java.util.UUID

inline fun <reified Entity : EntityInterface> KhomeSession.onStateChange(crossinline callback: Entity.(OldState?) -> Unit) =
    registerStateChangeEvent(callback)

inline fun <reified Entity : EntityInterface> KhomeSession.registerStateChangeEvent(
    crossinline callback: Entity.(OldState?) -> Unit
): LifeCycleHandler {
    val handle = UUID.randomUUID().toString()
    val entity = get<Entity>()

    val lifeCycleHandler = LifeCycleHandler(handle, entity)
    val stateChangeEvents: StateChangeEvents by inject()

    stateChangeEvents.subscribe(handle) {
        if (event.data.entityId == entity.id) {
            callback(entity, OldState(event.data.oldState!!))
        }
    }
    return lifeCycleHandler
}

class OldState(private val delegate: State) : StateInterface by delegate
