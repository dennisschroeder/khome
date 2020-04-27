package khome.core.events

import kotlinx.coroutines.coroutineScope

class HassEventRegistry(delegate: MutableMap<String, HassEvent> = mutableMapOf()) {
    private val registry = delegate
    fun register(eventName: String, event: HassEvent) {
        registry[eventName] = event
    }

    operator fun get(eventName: String) = registry[eventName]
    operator fun contains(eventName: String) = registry.contains(eventName)
    suspend fun forEach(operation: suspend (MutableMap.MutableEntry<String, HassEvent>) -> Unit) =
        coroutineScope {
            for (item in registry) {
                operation(item)
            }
        }
}
