package khome.events

import khome.observability.DefaultAsyncEventHandlerExceptionHandler
import khome.observability.Switchable
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
    override var enabled: AtomicBoolean = AtomicBoolean(true)
) : EventHandler<EventData>, Switchable {
    override fun handle(eventData: EventData) {
        if (!enabled.get()) return
        f(eventData, this)
    }
}

internal class AsyncEventHandler<EventData>(
    private val f: suspend CoroutineScope.(EventData, switchable: Switchable) -> Unit,
    override var enabled: AtomicBoolean = AtomicBoolean(true),
    context: CoroutineContext = Dispatchers.IO + DefaultAsyncEventHandlerExceptionHandler()
) : EventHandler<EventData>, Switchable {

    private val coroutineScope: CoroutineScope =
        CoroutineScope(context)

    override fun handle(eventData: EventData) {
        if (!enabled.get()) return
        coroutineScope.launch { f.invoke(this, eventData, this@AsyncEventHandler) }
    }
}
