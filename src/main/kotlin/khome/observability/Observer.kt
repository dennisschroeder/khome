package khome.observability

import khome.errorHandling.ObserverExceptionHandler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

interface Observer<S, A, H> {
    fun update(state: HistorySnapshot<S, A, H>)
}

interface SwitchableObserver<S, A> {
    val enabled: AtomicBoolean
}

interface Observable<S, A> {
    fun attachObserver(observer: SwitchableObserver<S, A>)
}

internal class ObserverImpl<S, A, H>(
    private val f: (snapshot: HistorySnapshot<S, A, H>, observer: SwitchableObserver<S, A>) -> Unit,
    private val exceptionHandler: ObserverExceptionHandler,
    override val enabled: AtomicBoolean = AtomicBoolean(true)
) : SwitchableObserver<S, A>, Observer<S, A, H> {
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
    private val f: suspend CoroutineScope.(snapshot: HistorySnapshot<S, A, H>, switchable: SwitchableObserver<S, A>) -> Unit,
    private val exceptionHandler: CoroutineExceptionHandler,
    override val enabled: AtomicBoolean = AtomicBoolean(true),
    context: CoroutineContext = Dispatchers.IO
) : SwitchableObserver<S, A>, Observer<S, A, H> {
    private val coroutineScope: CoroutineScope = CoroutineScope(context)
    override fun update(state: HistorySnapshot<S, A, H>) {
        if (!enabled.get()) return
        coroutineScope.launch(exceptionHandler) { f.invoke(this, state, this@AsyncObserverImpl) }
    }
}
