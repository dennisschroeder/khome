package khome.core.eventHandling

import kotlinx.coroutines.coroutineScope

class CustomEventRegistry(delegate: MutableMap<String, CustomEvent> = mutableMapOf()) {
    private val registry = delegate
    fun register(eventName: String, event: CustomEvent) {
        registry[eventName] = event
    }

    operator fun get(eventName: String) = registry[eventName]

    operator fun contains(eventName: String) = registry.contains(eventName)

    suspend fun forEach(operation: suspend (MutableMap.MutableEntry<String, CustomEvent>) -> Unit) = coroutineScope {
        for (item in registry) {
            operation(item)
        }
    }
}
