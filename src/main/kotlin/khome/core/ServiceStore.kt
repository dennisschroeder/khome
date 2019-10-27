package khome.core

import java.util.concurrent.ConcurrentHashMap

internal interface ServiceStoreInterface {
    val list: ConcurrentHashMap<String, List<String>>

    operator fun set(domain: String, services: List<String>): Unit
    operator fun get(domain: String) = list[domain]
    operator fun contains(domain: String): Boolean
}

internal class ServiceStore : Iterable<MutableMap.MutableEntry<String, List<String>>>, ServiceStoreInterface {
    override val list = ConcurrentHashMap<String, List<String>>()
    override operator fun iterator() = list.iterator()
    override operator fun set(domain: String, services: List<String>) {
        list[domain] = services
    }
    override operator fun contains(domain: String) = list.containsKey(domain)

    internal fun clear() = list.clear()
}
