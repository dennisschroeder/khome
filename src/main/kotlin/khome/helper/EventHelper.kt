package khome.helper

import khome.KhomeApplication
import khome.events.EventHandler
import kotlinx.coroutines.CoroutineScope

inline fun <reified ED> KhomeApplication.createAndAttachAsyncEventHandler(
    eventType: String,
    noinline f: suspend CoroutineScope.(ED) -> Unit
) {
    val eventHandler = createAsyncEventHandler(f)
    attachEventHandler(eventType, eventHandler)
}

inline fun <reified ED> KhomeApplication.createAndAttachEventHandler(eventType: String, noinline f: (ED) -> Unit) {
    val eventHandler = createEventHandler(f)
    attachEventHandler(eventType, eventHandler)
}

inline fun <reified ED> KhomeApplication.attachEventHandler(eventType: String, eventHandler: EventHandler<ED>) =
    attachEventHandler(eventType, eventHandler, ED::class)
