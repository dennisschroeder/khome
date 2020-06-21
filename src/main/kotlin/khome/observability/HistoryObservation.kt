package khome.observability

import khome.core.observing.CircularBuffer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface WithHistory<H> {
    val history: List<H>
}

interface WithState<S> {
    val state: S
}

interface WithAttributes<A> {
    val attributes: A
}

interface StateAndAttributes<S, A> : WithState<S>, WithAttributes<A>

internal class StateAndAttributesImpl<S, A>(
    override val state: S,
    override val attributes: A
) : StateAndAttributes<S, A>

/**
 * Represents the state, the attributes and an history with former state and attributes
 *
 * @property state the state object
 * @property attributes the attributes object
 * @property history the state and attributes history
 */
interface HistorySnapshot<S, A, H> : WithState<S>, WithAttributes<A>, WithHistory<H>

internal class HistorySnapshotIml<S, A, H>(
    override val state: S,
    override val attributes: A,
    override val history: List<H>
) : HistorySnapshot<S, A, H>

internal interface ObservableHistoryDelegate<S, H> : WithHistory<H>, ReadWriteProperty<Any?, S>

internal class ObservableHistoryNoInitialDelegate<S, SA>(
    private val observers: List<Observer<S, SA, StateAndAttributes<S, SA>>>,
    private val _history: CircularBuffer<StateAndAttributes<S, SA>>,
    private val attributes: () -> SA
) : ObservableHistoryDelegate<S, StateAndAttributes<S, SA>> {
    private var dirty: Boolean = false
    override val history: List<StateAndAttributes<S, SA>>
        get() = _history.snapshot()

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): S =
        _history.last()?.state ?: throw IllegalStateException("No value available yet.")

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: S) {
        if (dirty) observers.forEach { it.update(HistorySnapshotIml(value, attributes(), history)) }
        _history.add(StateAndAttributesImpl(value, attributes()))
        dirty = true
    }
}
