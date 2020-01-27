package khome.core.eventHandling

interface CustomEventInterface<EventType> {
    val eventType: String
    val listenerCount: Int
    fun subscribe(handle: String? = null, callback: EventType.() -> Unit)
    fun unsubscribe(handle: String)
    fun emit(eventData: EventType)
}
