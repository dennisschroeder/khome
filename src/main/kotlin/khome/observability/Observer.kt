package khome.observability

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

interface Observer<State> {
    var enabled: AtomicBoolean
    fun update(state: WithHistory<State>)
}

interface Observable<State> {
    fun attachObserver(observer: Observer<State>)
}

interface WithHistory<State> {
    val state: State
    val history: List<State>
}

interface ObservableHistory<State> : WithHistory<State>, Observable<State>

internal class ObserverImpl<T>(
    private val f: (WithHistory<T>) -> Unit,
    override var enabled: AtomicBoolean = AtomicBoolean(true)
) : Observer<T> {
    override fun update(state: WithHistory<T>) {
        if (!enabled.get()) return
        f(state)
    }
}

internal class AsyncObserver<S>(
    private val f: suspend CoroutineScope.(snapshot: WithHistory<S>) -> Unit,
    override var enabled: AtomicBoolean = AtomicBoolean(true),
    context: CoroutineContext = Dispatchers.IO + DefaultAsyncObserverExceptionHandler()
) : Observer<S>, CoroutineScope by CoroutineScope(context) {
    override fun update(state: WithHistory<S>) {
        if (!enabled.get()) return
        launch { f.invoke(this, state) }
    }
}
