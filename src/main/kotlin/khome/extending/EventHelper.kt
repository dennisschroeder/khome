package khome.extending

import khome.KhomeApplication
import khome.events.SwitchableEventHandler
import kotlinx.coroutines.CoroutineScope

inline fun <reified ED> KhomeApplication.createAndAttachAsyncEventHandler(
    eventType: String,
    noinline f: suspend CoroutineScope.(ED, SwitchableEventHandler<ED>) -> Unit
) {
    val eventHandler = AsyncEventHandler(f)
    attachEventHandler<ED>(eventType, eventHandler)
}

inline fun <reified ED> KhomeApplication.createAndAttachEventHandler(
    eventType: String,
    noinline f: (ED, SwitchableEventHandler<ED>) -> Unit
) {
    val eventHandler = EventHandler(f)
    attachEventHandler<ED>(eventType, eventHandler)
}

inline fun <reified ED> KhomeApplication.attachEventHandler(eventType: String, eventHandler: SwitchableEventHandler<ED>) =
    attachEventHandler(eventType, eventHandler, ED::class)
