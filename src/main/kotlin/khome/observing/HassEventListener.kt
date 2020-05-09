package khome.observing

import khome.KhomeComponent
import khome.core.events.EventData
import khome.core.events.EventListenerExceptionHandler
import khome.core.events.HassEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.get
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

inline fun <reified EventType : HassEvent> KhomeComponent.onHassEvent(
    context: CoroutineContext = Dispatchers.IO,
    noinline callback: suspend CoroutineScope.(EventData) -> Unit
): LifeCycleHandler = HassEventListener<EventType>(
    context = context,
    hassEvent = get(),
    exceptionHandler = get(),
    listener = callback
).lifeCycleHandler

class HassEventListener<EventType : HassEvent>(
    context: CoroutineContext,
    private val hassEvent: EventType,
    private val exceptionHandler: EventListenerExceptionHandler,
    private val listener: suspend CoroutineScope.(EventData) -> Unit
) : CoroutineScope by CoroutineScope(context) {

    private val handle = UUID.randomUUID().toString()
    val lifeCycleHandler = LifeCycleHandler(handle, hassEvent)
    private val stateListenerContext = HassEventListenerContext(hassEvent.eventType, handle, lifeCycleHandler)

    init {
        registerListener()
    }

    private fun registerListener() {
        hassEvent.subscribe(handle) { eventData ->
            launch(stateListenerContext + exceptionHandler) { listener(eventData) }
        }
    }
}

data class HassEventListenerContext(
    val eventType: String,
    val handle: String,
    val lifeCycleHandler: LifeCycleHandler
) : AbstractCoroutineContextElement(HassEventListenerContext) {
    companion object Key : CoroutineContext.Key<HassEventListenerContext>

    override fun toString(): String = "HassEventListener[$eventType]@$handle "
}
