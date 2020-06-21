package khome.events

import khome.errorHandling.EventHandlerExceptionHandler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

interface EventHandler<EventData> {
    fun handle(eventData: EventData)
}

interface SwitchableEventHandler<EventData> {
    val enabled: AtomicBoolean
}

internal class EventHandlerImpl<EventData>(
    private val f: (EventData, switchable: SwitchableEventHandler<EventData>) -> Unit,
    private val exceptionHandler: EventHandlerExceptionHandler,
    override val enabled: AtomicBoolean = AtomicBoolean(true)
) : EventHandler<EventData>, SwitchableEventHandler<EventData> {
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
    private val f: suspend CoroutineScope.(EventData, handler: SwitchableEventHandler<EventData>) -> Unit,
    private val exceptionHandler: CoroutineExceptionHandler,
    override val enabled: AtomicBoolean = AtomicBoolean(true),
    context: CoroutineContext = Dispatchers.IO
) : EventHandler<EventData>, SwitchableEventHandler<EventData> {

    private val coroutineScope: CoroutineScope =
        CoroutineScope(context)

    override fun handle(eventData: EventData) {
        if (!enabled.get()) return
        coroutineScope.launch(exceptionHandler) { f.invoke(this, eventData, this@AsyncEventHandlerImpl) }
    }
}
