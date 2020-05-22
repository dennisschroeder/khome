package khome.observability

import java.util.concurrent.atomic.AtomicBoolean

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
        if (!enabled.get()) {
            return
        }
        f(state)
    }
}
