package khome.observability

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.properties.ReadWriteProperty

interface Switchable {
    var enabled: AtomicBoolean
}

interface Observer<S, A, L> {
    fun update(state: WithHistory<S, A, L>)
}

interface SwitchableObserver<S, A, L> : Observer<S, A, L>, Switchable

interface Observable<State> {
    fun attachObserver(observer: Switchable)
}

interface WithHistory<S, A, L> {
    val state: S
    val attributes: A
    val history: List<L>
}

interface WithHistoryNoState<State> {
    val history: List<State>
}

interface ObservableHistoryDelegate<State, List> : WithHistoryNoState<List>, ReadWriteProperty<Any?, State>

internal class ObserverImpl<S, A, L>(
    private val f: (snapshot: WithHistory<S, A, L>, observer: Switchable) -> Unit,
    override var enabled: AtomicBoolean = AtomicBoolean(true)
) : SwitchableObserver<S, A, L> {
    override fun update(state: WithHistory<S, A, L>) {
        if (!enabled.get()) return
        f(state, this@ObserverImpl)
    }
}

internal class AsyncObserver<S, A, L>(
    private val f: suspend CoroutineScope.(snapshot: WithHistory<S, A, L>, switchable: Switchable) -> Unit,
    override var enabled: AtomicBoolean = AtomicBoolean(true),
    context: CoroutineContext = Dispatchers.IO + DefaultAsyncObserverExceptionHandler()
) : SwitchableObserver<S, A, L> {
    private val coroutineScope: CoroutineScope = CoroutineScope(context)
    override fun update(state: WithHistory<S, A, L>) {
        if (!enabled.get()) return
        coroutineScope.launch { f.invoke(this, state, this@AsyncObserver) }
    }
}
