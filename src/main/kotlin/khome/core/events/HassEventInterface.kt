package khome.core.events

import kotlinx.coroutines.CoroutineScope

interface HassEventInterface<EventDataType> {
    val eventType: String
    val listenerCount: Int
    fun subscribe(handle: String? = null, callback: suspend CoroutineScope.(EventDataType) -> Unit)
    fun unsubscribe(handle: String)
    suspend fun emit(eventData: EventDataType)
}
