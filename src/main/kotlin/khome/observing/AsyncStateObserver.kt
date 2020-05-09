package khome.observing

import khome.KhomeComponent
import khome.core.State
import khome.core.entities.EntitySubject
import khome.core.entities.StateHistorySnapshot
import khome.core.events.EntityObserverExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.get
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

typealias AsyncStateObserverSuspendable = suspend CoroutineScope.(StateHistorySnapshot, State) -> Unit

inline fun <reified Entity : EntitySubject<*>> KhomeComponent.onStateChange(
    context: CoroutineContext = Dispatchers.IO,
    noinline observer: AsyncStateObserverSuspendable
): Entity {
    val entity: Entity = get()
    val exceptionHandler: EntityObserverExceptionHandler = get()
    val handle: UUID = UUID.randomUUID()
    val observableContext = context + exceptionHandler + AsyncStateObserverContext(entity, handle)
    entity.registerObserver(handle, AsyncStateObserver(observableContext, observer))
    return entity
}

class AsyncStateObserver(context: CoroutineContext, private val observer: AsyncStateObserverSuspendable) :
    CoroutineScope by CoroutineScope(context) {
    operator fun invoke(stateHistorySnapshot: StateHistorySnapshot, new: State) = launch { observer(stateHistorySnapshot, new) }
}

data class AsyncStateObserverContext (
    val entity: EntitySubject<*>,
    val handle: UUID
) : AbstractCoroutineContextElement(AsyncStateObserverContext) {
    companion object Key : CoroutineContext.Key<AsyncStateObserverContext>

    fun disableObservable() = entity.removeObserver(handle)
    override fun toString(): String = "StateObservable[${entity.id}]@$handle "
}

fun CoroutineScope.disableListener() =
    coroutineContext[AsyncStateObserverContext]?.disableObservable()
        ?: throw IllegalStateException("No StateObservableContext found in coroutine. Could not disable observable")
