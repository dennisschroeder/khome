package khome.observability

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

interface Switchable {
    var enabled: AtomicBoolean
}

interface Observer<State> {
    fun update(state: WithHistory<State>)
}

interface SwitchableObserver<State> : Observer<State>, Switchable

interface Observable<State> {
    fun attachObserver(observer: Switchable)
}

interface WithHistory<State> {
    val state: State
    val history: List<State>
}

interface ObservableHistory<State> : WithHistory<State>, Observable<State>

internal class ObserverImpl<T>(
    private val f: (snapshot: WithHistory<T>, observer: Switchable) -> Unit,
    override var enabled: AtomicBoolean = AtomicBoolean(true)
) : SwitchableObserver<T> {
    override fun update(state: WithHistory<T>) {
        if (!enabled.get()) return
        f(state, this@ObserverImpl)
    }
}

internal class AsyncObserver<T>(
    private val f: suspend CoroutineScope.(snapshot: WithHistory<T>, switchable: Switchable) -> Unit,
    override var enabled: AtomicBoolean = AtomicBoolean(true),
    context: CoroutineContext = Dispatchers.IO + DefaultAsyncObserverExceptionHandler()
) : SwitchableObserver<T> {
    private val coroutineScope: CoroutineScope = CoroutineScope(context)
    override fun update(state: WithHistory<T>) {
        if (!enabled.get()) return
        coroutineScope.launch { f.invoke(this, state, this@AsyncObserver) }
    }
}
