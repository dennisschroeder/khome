package khome.extending.events

import khome.KhomeApplication
import khome.events.EventHandlerFunction
import khome.observability.Switchable

inline fun <reified ED> KhomeApplication.attachEventHandler(
    eventType: String,
    noinline eventHandler: EventHandlerFunction<ED>
): Switchable = attachEventHandler(eventType, ED::class, eventHandler)
