package khome.events

import khome.errorHandling.EventHandlerExceptionHandler
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

typealias EventHandlerFunction<EventData> = (EventData, switchable: Switchable) -> Unit
typealias AsyncEventHandlerFunction<EventData> = suspend CoroutineScope.(EventData, handler: Switchable) -> Unit

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

internal class AsyncEventHandlerImpl<EventData>(
    private val f: AsyncEventHandlerFunction<EventData>,
    private val exceptionHandler: CoroutineExceptionHandler,
    context: CoroutineContext = Dispatchers.IO
) : EventHandler<EventData>, Switchable {

    private val enabled: AtomicBoolean = AtomicBoolean(true)

    override fun enable() = enabled.set(true)
    override fun disable() = enabled.set(false)
    override fun isEnabled(): Boolean = enabled.get()

    private val coroutineScope: CoroutineScope = CoroutineScope(context)

    override fun handle(eventData: EventData) {
        if (!enabled.get()) return
        coroutineScope.launch(exceptionHandler) { f.invoke(this, eventData, this@AsyncEventHandlerImpl) }
    }
}
