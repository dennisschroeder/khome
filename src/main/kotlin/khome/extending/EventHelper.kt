package khome.extending

import khome.KhomeApplication
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineScope

inline fun <reified ED> KhomeApplication.createAndAttachAsyncEventHandler(
    eventType: String,
    noinline f: suspend CoroutineScope.(ED, Switchable) -> Unit
) {
    val eventHandler = AsyncEventHandler(f)
    attachEventHandler<ED>(eventType, eventHandler)
}

inline fun <reified ED> KhomeApplication.createAndAttachEventHandler(
    eventType: String,
    noinline f: (ED, Switchable) -> Unit
) {
    val eventHandler = EventHandler(f)
    attachEventHandler<ED>(eventType, eventHandler)
}

inline fun <reified ED> KhomeApplication.attachEventHandler(eventType: String, eventHandler: Switchable) =
    attachEventHandler(eventType, eventHandler, ED::class)
