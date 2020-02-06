package khome.core.eventHandling

interface HassEventInterface<EventDataType> {
    val eventType: String
    val listenerCount: Int
    fun subscribe(handle: String? = null, callback: suspend EventDataType.() -> Unit)
    fun unsubscribe(handle: String)
    suspend fun emit(eventData: EventDataType)
}
