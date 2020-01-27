package khome.core.eventHandling

import kotlinx.coroutines.coroutineScope

class CustomEventRegistry(delegate: MutableList<String> = mutableListOf()) {
    private val registry = delegate
    fun register(eventName: String) = registry.add(eventName)

    operator fun contains(eventName: String) = registry.contains(eventName)

    suspend fun forEach(operation: suspend (String) -> Unit) = coroutineScope {
        for (item in registry) {
            operation(item)
        }
    }
}
