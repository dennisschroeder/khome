package khome.errorHandling

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

typealias ExceptionHandlerFunction = (exception: Throwable) -> Unit

internal interface ExceptionHandler {
    fun handleExceptions(exception: Throwable)
}

internal class ObserverExceptionHandler(private val f: ExceptionHandlerFunction) : ExceptionHandler {
    override fun handleExceptions(exception: Throwable) {
        f(exception)
    }
}

internal class AsyncObserverExceptionHandler(private val f: ExceptionHandlerFunction) :
    AbstractCoroutineContextElement(CoroutineExceptionHandler), CoroutineExceptionHandler {
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        f(exception)
    }
}

internal class EventHandlerExceptionHandler(private val f: ExceptionHandlerFunction) : ExceptionHandler {
    override fun handleExceptions(exception: Throwable) {
        f(exception)
    }
}

internal class AsyncEventHandlerExceptionHandler(private val f: ExceptionHandlerFunction) :
    AbstractCoroutineContextElement(CoroutineExceptionHandler), CoroutineExceptionHandler {
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        f(exception)
    }
}
