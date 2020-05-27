package khome.observability

import kotlinx.coroutines.CoroutineExceptionHandler
import mu.KotlinLogging
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

internal class DefaultAsyncObserverExceptionHandler : AbstractCoroutineContextElement(CoroutineExceptionHandler),
    EntityObserverExceptionHandler {
    private val logger = KotlinLogging.logger { }
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        logger.error(exception) { "Caught Exception in observer" }
    }
}

interface EntityObserverExceptionHandler : CoroutineExceptionHandler

internal class DefaultAsyncEventHandlerExceptionHandler : AbstractCoroutineContextElement(CoroutineExceptionHandler),
    EventHandlerExceptionHandler {
    private val logger = KotlinLogging.logger { }
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        logger.error(exception) { "Caught Exception in observer" }
    }
}

interface EventHandlerExceptionHandler : CoroutineExceptionHandler
