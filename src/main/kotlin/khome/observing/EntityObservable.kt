package khome.observing

import khome.KhomeComponent
import khome.core.State
import khome.core.entities.EntitySubject
import khome.core.events.EntityObserverExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.get
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

typealias EntityObservableFunction = suspend CoroutineScope.(State, State) -> Unit

inline fun <reified Entity : EntitySubject<*>> KhomeComponent.onStateChange(
    context: CoroutineContext = Dispatchers.IO,
    noinline observable: EntityObservableFunction
): Entity {
    val entity: Entity = get()
    val exceptionHandler: EntityObserverExceptionHandler = get()
    val handle: String = UUID.randomUUID().toString()
    val observableContext = context + exceptionHandler + EntityObservableContext(entity, handle)
    entity[handle] = EntityObservable(observableContext, observable)
    return entity
}

class EntityObservable(context: CoroutineContext, private val observable: EntityObservableFunction) :
    CoroutineScope by CoroutineScope(context) {
    operator fun invoke(oldState: State, newState: State) = launch { observable(oldState, newState) }
}

data class EntityObservableContext(
    val entity: EntitySubject<*>,
    val handle: String
) : AbstractCoroutineContextElement(EntityObservableContext) {
    companion object Key : CoroutineContext.Key<EntityObservableContext>

    fun disableObservable() = entity.removeObservable(handle)
    override fun toString(): String = "StateObservable[${entity.id}]@$handle "
}

fun CoroutineScope.disableListener() =
    coroutineContext[EntityObservableContext]?.disableObservable()
        ?: throw IllegalStateException("No StateObservableContext found in coroutine. Could not disable observable")
