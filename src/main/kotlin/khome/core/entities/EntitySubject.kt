package khome.core.entities

import io.ktor.util.KtorExperimentalAPI
import khome.core.State
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.exceptions.InvalidStateValueTypeException
import khome.observing.EntityObservable
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.inject
import java.time.OffsetDateTime
import kotlin.properties.Delegates.observable
import kotlin.reflect.KClass

@Suppress("MemberVisibilityCanBePrivate", "PropertyName")
@KtorExperimentalAPI
@ObsoleteCoroutinesApi
abstract class EntitySubject<StateValueType>(
    override val domain: String,
    override val name: String
) : KhomeKoinComponent, EntitySubjectInterface, Iterable<Map.Entry<String, EntityObservable>> {

    final override val id: String get() = "$domain.$name"
    private val initialState = State(id, OffsetDateTime.now(), "initial", emptyMap(), OffsetDateTime.now())
    private val entityIdToEntityTypeMap: EntityIdToEntityTypeMap by inject()
    internal var _state: State by observable(initialState) { _, oldState, newState ->
        forEach { func -> func.value(oldState, newState) }
    }

    override val state get() = _state

    private val observables: HashMap<String, EntityObservable> = hashMapOf()
    final override fun iterator(): Iterator<Map.Entry<String, EntityObservable>> = observables.iterator()
    operator fun set(handle: String, observable: EntityObservable) {
        observables[handle] = observable
    }

    fun removeObservable(handle: String) {
        observables.remove(handle)
    }

    init {
        @Suppress("UNCHECKED_CAST")
        if (_state.state as? StateValueType == null) throw InvalidStateValueTypeException("Could not cast new state vale to type parameter of entity: $id ")
        entityIdToEntityTypeMap[id] = this::class
    }

    override fun toString() = id
}

class EntityIdToEntityTypeMap(delegate: HashMap<String, KClass<*>>) : Iterable<Map.Entry<String, KClass<*>>> {
    private val map = delegate
    override operator fun iterator(): Iterator<Map.Entry<String, KClass<*>>> = map.iterator()
    operator fun get(entityId: String) = map[entityId]
    operator fun set(entityId: String, type: KClass<*>) {
        map[entityId] = type
    }
}
