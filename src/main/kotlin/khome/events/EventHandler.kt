package khome.events

import khome.observability.DefaultAsyncEventHandlerExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

interface EventHandler<EventData> {
    var enabled: AtomicBoolean
    fun handle(eventData: EventData)
}

internal class EventHandlerImpl<EventData>(
    private val f: (EventData) -> Unit,
    override var enabled: AtomicBoolean = AtomicBoolean(true)
) : EventHandler<EventData> {
    override fun handle(eventData: EventData) {
        if (!enabled.get()) return
        f(eventData)
    }
}

internal class AsyncEventHandler<EventData>(
    private val f: suspend CoroutineScope.(EventData) -> Unit,
    override var enabled: AtomicBoolean = AtomicBoolean(true),
    context: CoroutineContext = Dispatchers.IO + DefaultAsyncEventHandlerExceptionHandler()
) : EventHandler<EventData>, CoroutineScope by CoroutineScope(context) {
    override fun handle(eventData: EventData) {
        if (!enabled.get()) return
        launch { f.invoke(this, eventData) }
    }
}
