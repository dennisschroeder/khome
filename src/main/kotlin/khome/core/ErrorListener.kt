package khome.core

import io.ktor.util.KtorExperimentalAPI
import khome.KhomeComponent
import khome.core.events.ErrorResponseEvent
import khome.core.events.EventListenerExceptionHandler
import khome.listening.LifeCycleHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.core.get
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
fun KhomeComponent.onErrorResponse(
    context: CoroutineContext = Dispatchers.IO,
    callback: suspend CoroutineScope.(ResultResponse) -> Unit
): LifeCycleHandler =
    ErrorResponseListener(
        context = context,
        errorResponseEvent = get(),
        exceptionHandler = get(),
        listener = callback
    ).lifeCycleHandler

class ErrorResponseListener(
    context: CoroutineContext,
    private val errorResponseEvent: ErrorResponseEvent,
    private val exceptionHandler: EventListenerExceptionHandler,
    private val listener: suspend CoroutineScope.(ResultResponse) -> Unit
) : CoroutineScope by CoroutineScope(context) {
    private val handle = UUID.randomUUID().toString()
    val lifeCycleHandler = LifeCycleHandler(handle, errorResponseEvent)

    init {
        registerListener()
    }

    private fun registerListener() {
        errorResponseEvent.subscribe(handle) { eventData ->
            launch(ErrorResponseListenerContext(eventData.id, handle, lifeCycleHandler) + exceptionHandler) { listener(eventData) }
        }
    }
}

data class ErrorResponseListenerContext(
    val callerId: Int,
    val handle: String,
    val lifeCycleHandler: LifeCycleHandler
) : AbstractCoroutineContextElement(ErrorResponseListenerContext) {
    companion object Key : CoroutineContext.Key<ErrorResponseListenerContext>

    override fun toString(): String = "ErrorResponseListener@$handle "
}
