package khome.listening

import khome.KhomeComponent
import khome.core.entities.EntityInterface
import khome.core.events.StateChangeEvent
import khome.core.events.StateChangeListenerExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.get
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

inline fun <reified Entity : EntityInterface> KhomeComponent.onStateChange(
    context: CoroutineContext = Dispatchers.IO,
    noinline callback: suspend CoroutineScope.(Entity) -> Unit
): LifeCycleHandler {

    val listener = StateListener(
        context = context,
        stateChangeEvent = get(),
        entity = get(),
        exceptionHandler = get(),
        listener = callback
    )

    return listener.lifeCycleHandler
}

class StateListener<Entity : EntityInterface>(
    context: CoroutineContext,
    private val stateChangeEvent: StateChangeEvent,
    private val entity: Entity,
    private val exceptionHandler: StateChangeListenerExceptionHandler,
    private val listener: suspend CoroutineScope.(Entity) -> Unit
) : CoroutineScope by CoroutineScope(context) {

    private val handle = UUID.randomUUID().toString()
    val lifeCycleHandler = LifeCycleHandler(handle, stateChangeEvent)
    private val stateListenerContext = StateListenerContext(entity.id, handle, lifeCycleHandler)

    init {
        registerListener()
    }

    private fun registerListener() {
        stateChangeEvent.subscribe(handle) { stateChangeResponse ->
            if (stateChangeResponse.event.data.entityId == entity.id)
                launch(stateListenerContext + exceptionHandler) { listener(this, entity) }
        }
    }
}

data class StateListenerContext(
    val entityId: String,
    val handle: String,
    val lifeCycleHandler: LifeCycleHandler
) : AbstractCoroutineContextElement(StateListenerContext) {
    companion object Key : CoroutineContext.Key<StateListenerContext>

    override fun toString(): String = "StateListener[$entityId]@$handle "
}

fun CoroutineScope.disableListener() =
    coroutineContext[StateListenerContext]?.lifeCycleHandler?.disable() ?: throw IllegalStateException("No StateListenerContext in coroutine context.")
