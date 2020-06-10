package khome.observability

import khome.core.observing.CircularBuffer
import kotlin.reflect.KProperty

interface StateWithAttributes<S, SA> {
    val state: S
    val attributes: SA
}

internal class StateWithAttributesImpl<S, SA>(
    override val state: S,
    override val attributes: SA
): StateWithAttributes<S, SA>

internal class HistorySnapshot<S, A, L>(
    override val state: S,
    override val attributes: A,
    override val history: List<L>
) : WithHistory<S, A, L>

internal class ObservableHistoryNoInitialDelegate<S, SA>(
    private val observers: List<SwitchableObserver<S, SA, StateWithAttributes<S, SA>>>,
    maxHistory: Int = 10,
    private val attributes: () -> SA
) : ObservableHistoryDelegate<S, StateWithAttributes<S, SA>> {
    private var dirty: Boolean = false
    override val history: List<StateWithAttributes<S, SA>>
        get() = _history.snapshot()

    private val _history = CircularBuffer<StateWithAttributes<S, SA>>(maxHistory)

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): S =
        _history.last()?.state ?: throw IllegalStateException("No value available yet.")

     override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: S) {
        if (dirty) observers.forEach { it.update(HistorySnapshot(value,attributes() , history)) }
        _history.add(StateWithAttributesImpl(value, attributes()))
        dirty = true
    }
}
