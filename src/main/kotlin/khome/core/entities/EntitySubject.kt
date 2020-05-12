package khome.core.entities

import io.ktor.util.KtorExperimentalAPI
import khome.core.State
import khome.core.StateAttributes
import khome.core.StateResponse
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.events.EntityObserverExceptionHandler
import khome.observing.AsyncStateObserver
import khome.observing.AsyncStateObserverContext
import khome.observing.AsyncStateObserverSuspendable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.get
import org.koin.core.inject
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

typealias StateHistory<StateValueType> = CircularBuffer<State<StateValueType>>
typealias StateHistorySnapshot<StateValueType> = List<State<StateValueType>>

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
abstract class EntitySubject<StateValueType>(
    final override val entityId: EntityId,
    historyCapacity: Int = 10
) : KhomeKoinComponent, EntitySubjectInterface {

    private val entityIdToEntityTypeMap: EntityIdToEntityTypeMap by inject()

    internal var state: State<StateValueType>
        get() = stateHistory.last() ?: throw IllegalStateException("No state available yet.")
        set(newState) {
            val historySnapshot = stateHistory.snapshot()
            observers.forEach { it.value(historySnapshot, newState) }
            stateHistory.add(newState)
        }

    init {
        entityIdToEntityTypeMap[entityId] = this::class
    }

    fun onStateChange(
        context: CoroutineContext = Dispatchers.IO,
        observer: AsyncStateObserverSuspendable<StateValueType>
    ) {
        val exceptionHandler: EntityObserverExceptionHandler = get()
        val handle: UUID = UUID.randomUUID()
        val observableContext = context + exceptionHandler + AsyncStateObserverContext(this, handle)
        registerObserver(handle, AsyncStateObserver(observableContext, observer))
    }

    val stateValue
        get() = state.value

    final override val attributes: StateAttributes
        get() = state.attributes

    final val lastUpdated: OffsetDateTime
        get() = state.lastUpdated

    final val lastChanged: OffsetDateTime
        get() = state.lastChanged

    private val observers: HashMap<UUID, AsyncStateObserver<StateValueType>> = hashMapOf()
    val observerCount = observers.size
    fun registerObserver(handle: UUID, asyncStateObserver: AsyncStateObserver<StateValueType>) {
        observers[handle] = asyncStateObserver
    }

    fun removeObserver(handle: UUID) {
        observers.remove(handle)
    }

    internal fun setStateFromResponse(response: StateResponse) {
        assert(response.entityId == entityId)
        state = State(
            value = response.state as StateValueType,
            attributes = response.attributes,
            lastUpdated = response.lastUpdated,
            lastChanged = response.lastChanged
        )
    }

    private val stateHistory = StateHistory<StateValueType>(historyCapacity)

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

    fun last() = backend.lastOrNull()
    fun snapshot() = backend.toList()
}
