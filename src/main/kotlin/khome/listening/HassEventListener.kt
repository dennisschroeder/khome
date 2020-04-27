package khome.listening

import khome.core.KhomeComponent
import khome.core.events.EventData
import khome.core.events.HassEvent
import kotlinx.coroutines.CoroutineScope
import org.koin.core.get
import java.util.UUID

inline fun <reified EventType : HassEvent> KhomeComponent.onHassEvent(noinline callback: suspend CoroutineScope.(EventData) -> Unit): LifeCycleHandler =
    registerHassEventCallback<EventType>(callback)

inline fun <reified EventType : HassEvent> KhomeComponent.registerHassEventCallback(
    noinline callback: suspend CoroutineScope.(EventData) -> Unit
): LifeCycleHandler {
    val handle = UUID.randomUUID().toString()
    val event = get<EventType>()
    event.subscribe(handle, callback)
    return LifeCycleHandler(handle)
}
