package khome.core.events

interface EventInterface<EventDataType> {
    val listenerCount: Int
    fun subscribe(handle: String? = null, callback: (EventDataType) -> Unit)
    fun unsubscribe(handle: String)
    suspend fun emit(eventData: EventDataType)
}
