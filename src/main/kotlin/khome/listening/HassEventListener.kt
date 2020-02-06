package khome.listening

import khome.KhomeSession
import khome.core.eventHandling.EventData
import khome.core.eventHandling.HassEvent
import org.koin.core.get
import java.util.UUID

inline fun <reified EventType : HassEvent> KhomeSession.onHassEvent(noinline callback: suspend (EventData) -> Unit): LifeCycleHandler =
    registerHassEventCallback<EventType>(callback)

inline fun <reified EventType : HassEvent> KhomeSession.registerHassEventCallback(
    noinline callback: suspend (EventData) -> Unit
): LifeCycleHandler {
    val handle = UUID.randomUUID().toString()
    val event = get<EventType>()
    event.subscribe(handle, callback)
    return LifeCycleHandler(handle)
}
