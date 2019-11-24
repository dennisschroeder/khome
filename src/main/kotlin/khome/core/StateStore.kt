package khome.core

import java.util.Collections

interface StateStoreInterface {
    val list: Map<String, State>
    val listenerCount: Int

    operator fun set(entityId: String, state: State): Unit
    operator fun get(entityId: String) = list[entityId]
    operator fun contains(entityId: String): Boolean
    fun clear()
}

internal class StateStore : Iterable<MutableMap.MutableEntry<String, State>>, StateStoreInterface {
    override val listenerCount: Int
        get() = list.size
    override val list: MutableMap<String, State> = Collections.synchronizedMap(HashMap<String, State>())
    override operator fun iterator() = list.iterator()
    override operator fun set(entityId: String, state: State) {
        list[entityId] = state
    }
    override operator fun contains(entityId: String) = list.containsKey(entityId)

    override fun clear() = list.clear()
}
