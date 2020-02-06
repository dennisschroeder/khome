package khome.core.eventHandling

internal interface EventInterface<EventDataType> {
    val listenerCount: Int
    fun subscribe(handle: String? = null, callback: suspend EventDataType.() -> Unit)
    fun unsubscribe(handle: String)
    suspend fun emit(eventData: EventDataType)
}
