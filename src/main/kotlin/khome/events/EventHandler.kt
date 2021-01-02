package khome.events

import khome.errorHandling.EventHandlerExceptionHandler
import khome.observability.Switchable
import java.util.concurrent.atomic.AtomicBoolean

typealias EventHandlerFunction<EventData> = (EventData, switchable: Switchable) -> Unit

interface EventHandler<EventData> {
    fun handle(eventData: EventData)
}

internal class EventHandlerImpl<EventData>(
    private val f: EventHandlerFunction<EventData>,
    private val exceptionHandler: EventHandlerExceptionHandler
) : EventHandler<EventData>, Switchable {

    private val enabled: AtomicBoolean = AtomicBoolean(true)

    override fun enable() = enabled.set(true)
    override fun disable() = enabled.set(false)
    override fun isEnabled(): Boolean = enabled.get()

    override fun handle(eventData: EventData) {
        if (!enabled.get()) return
        try {
            f(eventData, this)
        } catch (e: Throwable) {
            exceptionHandler.handleExceptions(e)
        }
    }
}
