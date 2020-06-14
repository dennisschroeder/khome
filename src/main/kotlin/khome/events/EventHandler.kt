package khome.events

import khome.errorHandling.EventHandlerExceptionHandler
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

interface EventHandler<EventData> {
    fun handle(eventData: EventData)
}

internal class EventHandlerImpl<EventData>(
    private val f: (EventData, switchable: Switchable) -> Unit,
    private val exceptionHandler: EventHandlerExceptionHandler,
    override var enabled: AtomicBoolean = AtomicBoolean(true)
) : EventHandler<EventData>, Switchable {
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
    private val f: suspend CoroutineScope.(EventData, switchable: Switchable) -> Unit,
    private val exceptionHandler: CoroutineExceptionHandler,
    override var enabled: AtomicBoolean = AtomicBoolean(true),
    context: CoroutineContext = Dispatchers.IO
) : EventHandler<EventData>, Switchable {

    private val coroutineScope: CoroutineScope =
        CoroutineScope(context)

    override fun handle(eventData: EventData) {
        if (!enabled.get()) return
        coroutineScope.launch(exceptionHandler) { f.invoke(this, eventData, this@AsyncEventHandlerImpl) }
    }
}
