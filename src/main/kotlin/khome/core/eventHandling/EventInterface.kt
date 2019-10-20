package khome.core.eventHandling

internal interface EventInterface<EventType> {
    val stateListenerCount: Int
    fun subscribe(handle: String? = null, callback: EventType.() -> Unit)
    fun unsubscribe(handle: String): List<Any>
    fun emit(eventData: EventType)
}
