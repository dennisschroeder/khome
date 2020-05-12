package khome.observing

import khome.core.State
import khome.core.entities.EntitySubject
import khome.core.entities.StateHistorySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

typealias AsyncStateObserverSuspendable<StateValueType> = suspend CoroutineScope.(StateHistorySnapshot<StateValueType>, State<StateValueType>) -> Unit

class AsyncStateObserver<StateValueType>(
    context: CoroutineContext,
    private val observer: AsyncStateObserverSuspendable<StateValueType>
) :
    CoroutineScope by CoroutineScope(context) {
    operator fun invoke(stateHistorySnapshot: StateHistorySnapshot<StateValueType>, new: State<StateValueType>) =
        launch { observer(stateHistorySnapshot, new) }
}

data class AsyncStateObserverContext(
    val entity: EntitySubject<*>,
    val handle: UUID
) : AbstractCoroutineContextElement(AsyncStateObserverContext) {
    companion object Key : CoroutineContext.Key<AsyncStateObserverContext>

    fun disableObservable() = entity.removeObserver(handle)
    override fun toString(): String = "StateObservable[${entity.entityId}]@$handle "
}

fun CoroutineScope.disableListener() =
    coroutineContext[AsyncStateObserverContext]?.disableObservable()
        ?: throw IllegalStateException("No StateObservableContext found in coroutine. Could not disable observable")
