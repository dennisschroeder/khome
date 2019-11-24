package khome.core.eventHandling

internal interface EventInterface<EventType> {
    val listenerCount: Int
    fun subscribe(handle: String? = null, callback: EventType.() -> Unit)
    fun unsubscribe(handle: String)
    fun emit(eventData: EventType)
}
