package khome.extending

import khome.KhomeApplication
import khome.events.AsyncEventHandlerFunction
import khome.events.EventHandlerFunction
import khome.observability.Switchable

inline fun <reified ED> KhomeApplication.attachEventHandler(
    eventType: String,
    noinline eventHandler: EventHandlerFunction<ED>
): Switchable =
    attachEventHandler(eventType, eventHandler, ED::class)

inline fun <reified ED> KhomeApplication.attachAsyncEventHandler(
    eventType: String,
    noinline eventHandler: AsyncEventHandlerFunction<ED>
): Switchable =
    attachAsyncEventHandler(eventType, eventHandler, ED::class)
