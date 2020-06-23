package khome.observability

import khome.errorHandling.ObserverExceptionHandler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

typealias ObserverFunction<S, A, H> = (snapshot: HistorySnapshot<S, A, H>, observer: Switchable) -> Unit
typealias AsyncObserverFunction<S, A, H> = suspend CoroutineScope.(snapshot: HistorySnapshot<S, A, H>, switchable: Switchable) -> Unit

internal interface Observer<S, A, H> {
    fun update(state: HistorySnapshot<S, A, H>)
}

/**
 * A Switchable instance
 *
 * Controls the observer function execution
 * Is enabled by default
 */
interface Switchable {
    /**
     * Returns the current state
     * @return Boolean
     */
    fun isEnabled(): Boolean

    /**
     * Enables the observer function execution
     */
    fun enable()

    /**
     * Disables the observer function execution
     */
    fun disable()
}

interface Observable<S, A> {
    fun attachObserver(observer: ObserverFunction<S, A, StateAndAttributes<S, A>>): Switchable
    fun attachAsyncObserver(observer: AsyncObserverFunction<S, A, StateAndAttributes<S, A>>): Switchable
}

internal class ObserverImpl<S, A, H>(
    private val f: ObserverFunction<S, A, H>,
    private val exceptionHandler: ObserverExceptionHandler
) : Switchable, Observer<S, A, H> {

    private val enabled: AtomicBoolean = AtomicBoolean(true)

    override fun enable() = enabled.set(true)
    override fun disable() = enabled.set(false)
    override fun isEnabled(): Boolean = enabled.get()

    override fun update(state: HistorySnapshot<S, A, H>) {
        if (!enabled.get()) return
        try {
            f(state, this@ObserverImpl)
        } catch (e: Throwable) {
            exceptionHandler.handleExceptions(e)
        }
    }
}

internal class AsyncObserverImpl<S, A, H>(
    private val f: AsyncObserverFunction<S, A, H>,
    private val exceptionHandler: CoroutineExceptionHandler,
    context: CoroutineContext = Dispatchers.IO
) : Switchable, Observer<S, A, H> {

    private val enabled: AtomicBoolean = AtomicBoolean(true)

    override fun enable() = enabled.set(true)
    override fun disable() = enabled.set(false)
    override fun isEnabled(): Boolean = enabled.get()

    private val coroutineScope: CoroutineScope = CoroutineScope(context)
    override fun update(state: HistorySnapshot<S, A, H>) {
        if (!enabled.get()) return
        coroutineScope.launch(exceptionHandler) { f.invoke(this, state, this@AsyncObserverImpl) }
    }
}
