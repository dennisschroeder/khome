package khome.core.entities

import io.ktor.util.KtorExperimentalAPI
import khome.core.State
import khome.core.StateAttributes
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.exceptions.InvalidStateValueTypeException
import khome.observing.AsyncStateObserver
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.inject
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.properties.Delegates.observable
import kotlin.reflect.KClass

typealias StateHistory = CircularBuffer<State>
typealias StateHistorySnapshot = List<State>

data class EntityId(private val domain: String, private val id: String) {
    override fun toString(): String = "$domain.$id"

    companion object {
        fun fromString(entityId: String): EntityId {
            val domain: String = entityId.split(".").first()
            val id: String = entityId.split(".").last()
            return EntityId(domain, id)
        }
    }
}

@Suppress("MemberVisibilityCanBePrivate", "PropertyName")
@OptIn(ObsoleteCoroutinesApi::class, KtorExperimentalAPI::class)
abstract class EntitySubject<StateValueType : Any>(
    final override val domain: String,
    final override val id: String,
    historyCapacity: Int = 10
) : KhomeKoinComponent, EntitySubjectInterface {

    private val entityIdToEntityTypeMap: EntityIdToEntityTypeMap by inject()
    final override val entityId: EntityId
        get() = EntityId(domain, id)

    private val initialState = State(entityId, OffsetDateTime.now(), "initial", emptyMap(), OffsetDateTime.now())

    internal var _state: State by observable(initialState) { _, oldState, newState ->
        if (oldState.state != "initial") stateHistory.add(oldState)
        val historySnapshot: StateHistorySnapshot = stateHistory.snapshot()
        observers.forEach { mapEntry -> mapEntry.value(historySnapshot, newState) }
    }

    init {
        @Suppress("UNCHECKED_CAST")
        if (_state.state as? StateValueType == null) throw InvalidStateValueTypeException("Could not cast new state val to type parameter of entity: $id ")
        entityIdToEntityTypeMap[entityId] = this::class
    }

    @Suppress("UNCHECKED_CAST")
    override val state: StateValueType
        get() = _state.state as? StateValueType
            ?: throw InvalidStateValueTypeException("Could not cast new state val to type parameter of entity: $id")

    private fun getStateSnapshot(): State = _state.copy()

    val attributes: StateAttributes
        get() = getStateSnapshot().attributes

    val lastUpdated: OffsetDateTime
        get() = getStateSnapshot().lastUpdated

    val lastChanged: OffsetDateTime
        get() = getStateSnapshot().lastChanged

    private val observers: HashMap<UUID, AsyncStateObserver> = hashMapOf()
    val observerCount = observers.size
    fun registerObserver(handle: UUID, asyncStateObserver: AsyncStateObserver) {
        observers[handle] = asyncStateObserver
    }

    fun removeObserver(handle: UUID) {
        observers.remove(handle)
    }

    private val stateHistory = StateHistory(historyCapacity)

    override fun toString() = entityId.toString()
}

class EntityIdToEntityTypeMap(private val map: HashMap<EntityId, KClass<*>>) :
    Iterable<Map.Entry<EntityId, KClass<*>>> {
    val size get() = map.size
    override operator fun iterator(): Iterator<Map.Entry<EntityId, KClass<*>>> = map.iterator()
    operator fun get(entityId: EntityId) = map[entityId]
    operator fun set(entityId: EntityId, type: KClass<*>) {
        map[entityId] = type
    }
}

class CircularBuffer<E>(private val maxCapacity: Int) {
    private val backend: MutableList<E> = mutableListOf()
    fun add(e: E) =
        backend.add(e).also {
            if (backend.size > maxCapacity) {
                backend.removeAt(backend.size - 1)
            }
        }

    fun snapshot() = backend.toList()
}
