package khome.core.entities

import io.ktor.util.KtorExperimentalAPI
import khome.core.State
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.exceptions.InvalidStateValueTypeException
import khome.observing.ObservableCoroutine
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.inject
import java.time.OffsetDateTime
import kotlin.properties.Delegates.observable
import kotlin.reflect.KClass

@Suppress("MemberVisibilityCanBePrivate", "PropertyName")
@OptIn(ObsoleteCoroutinesApi::class, KtorExperimentalAPI::class, ExperimentalStdlibApi::class)
abstract class EntitySubject<StateValueType>(
    override val domain: String,
    override val name: String
) : KhomeKoinComponent, EntitySubjectInterface {

    private val entityIdToEntityTypeMap: EntityIdToEntityTypeMap by inject()
    final override val id: String get() = "$domain.$name"

    private val initialState = State(id, OffsetDateTime.now(), "initial", emptyMap(), OffsetDateTime.now())
    internal var _state: State by observable(initialState) { _, old, new ->
        _stateArchive.archive(old)
        observables.forEach { func -> func.value(old, new) }
    }

    override val state get() = _state

    private val _stateArchive = StateArchive<State>(10)
    private val observables: HashMap<String, ObservableCoroutine> = hashMapOf()
    val size = observables.size
    operator fun set(handle: String, observable: ObservableCoroutine) {
        observables[handle] = observable
    }

    fun removeObservable(handle: String) {
        observables.remove(handle)
    }

    init {
        @Suppress("UNCHECKED_CAST")
        if (_state.state as? StateValueType == null) throw InvalidStateValueTypeException("Could not cast new state val to type parameter of entity: $id ")
        entityIdToEntityTypeMap[id] = this::class
    }

    override fun toString() = id
}

class EntityIdToEntityTypeMap(private val map: HashMap<String, KClass<*>>) : Iterable<Map.Entry<String, KClass<*>>> {
    val size get() = map.size
    override operator fun iterator(): Iterator<Map.Entry<String, KClass<*>>> = map.iterator()
    operator fun get(entityId: String) = map[entityId]
    operator fun set(entityId: String, type: KClass<*>) {
        map[entityId] = type
    }
}

@OptIn(ExperimentalStdlibApi::class)
class StateArchive<StateType>(maxCapacity: Int, delegate: ArrayDeque<StateType> = ArrayDeque(maxCapacity)) :
    Iterable<StateType> {
    private val backend: ArrayDeque<StateType> = delegate

    private val limit: Int = maxCapacity
    val size: Int get() = backend.size

    private val limitExceeded: Boolean get() = size > limit

    fun archive(element: StateType) {
        backend.addFirst(element)
        if (limitExceeded) backend.removeLast().also { println("Removed oldest item") }
    }

    operator fun get(index: Int) = backend.getOrNull(index)
    override fun iterator(): Iterator<StateType> = backend.iterator()
}
