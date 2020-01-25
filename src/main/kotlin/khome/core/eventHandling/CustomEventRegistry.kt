package khome.core.eventHandling

class CustomEventRegistry(delegate: MutableList<String> = mutableListOf()) {
    private val registry = delegate
    fun register(eventName: String) = registry.add(eventName)

    operator fun contains(eventName: String) = registry.contains(eventName)

    fun forEach(operation: (String) -> Unit) = registry.forEach(operation)
}
