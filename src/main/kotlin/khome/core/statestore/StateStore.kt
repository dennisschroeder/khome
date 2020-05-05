package khome.core.statestore

import khome.core.State
import java.util.Collections

interface StateStoreInterface {
    val list: Map<String, StateStoreEntry>
    val listenerCount: Int

    operator fun set(entityId: String, state: StateStoreEntry)
    operator fun get(entityId: String) = list[entityId]
    operator fun contains(entityId: String): Boolean
    fun clear()
}

internal class StateStore : Iterable<MutableMap.MutableEntry<String, StateStoreEntry>>,
    StateStoreInterface {
    override val listenerCount: Int
        get() = list.size
    override val list: MutableMap<String, StateStoreEntry> =
        Collections.synchronizedMap(HashMap<String, StateStoreEntry>())

    override operator fun iterator() = list.iterator()
    override operator fun set(entityId: String, state: StateStoreEntry) {
        list[entityId] = state
    }

    override operator fun contains(entityId: String) = list.containsKey(entityId)

    override fun clear() = list.clear()
}

data class StateStoreEntry(val oldState: State, val newState: State)
