package khome.observability

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

interface Switchable {
    var enabled: AtomicBoolean
}

interface Observer<S, A, H> {
    fun update(state: HistorySnapshot<S, A, H>)
}

interface SwitchableObserver<S, A, H> : Observer<S, A, H>, Switchable

interface Observable {
    fun attachObserver(observer: Switchable)
}

internal class ObserverImpl<S, A, H>(
    private val f: (snapshot: HistorySnapshot<S, A, H>, observer: Switchable) -> Unit,
    override var enabled: AtomicBoolean = AtomicBoolean(true)
) : SwitchableObserver<S, A, H> {
    override fun update(state: HistorySnapshot<S, A, H>) {
        if (!enabled.get()) return
        f(state, this@ObserverImpl)
    }
}

internal class AsyncObserverImpl<S, A, H>(
    private val f: suspend CoroutineScope.(snapshot: HistorySnapshot<S, A, H>, switchable: Switchable) -> Unit,
    override var enabled: AtomicBoolean = AtomicBoolean(true),
    context: CoroutineContext = Dispatchers.IO + DefaultAsyncObserverExceptionHandler()
) : SwitchableObserver<S, A, H> {
    private val coroutineScope: CoroutineScope = CoroutineScope(context)
    override fun update(state: HistorySnapshot<S, A, H>) {
        if (!enabled.get()) return
        coroutineScope.launch { f.invoke(this, state, this@AsyncObserverImpl) }
    }
}
