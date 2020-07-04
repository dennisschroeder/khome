package khome.observability

import khome.core.observing.CircularBuffer
import khome.entities.Attributes
import khome.entities.State
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

internal interface ObservableDelegate<S, H> : ReadWriteProperty<Any?, S>

internal class ObservableDelegateNoInitial<S : State<*>, A : Attributes, E>(
    private val entity: E,
    private val observers: List<Observer<E>>,
    private val history: CircularBuffer<StateAndAttributes<S, A>>
) : ObservableDelegate<S, StateAndAttributes<S, A>> {
    private var dirty: Boolean = false

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): S =
        history.first?.state ?: throw IllegalStateException("No value available yet.")

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: S) {
        @Suppress("UNCHECKED_CAST")
        history.addFirst(StateAndAttributesImpl(value, (entity as WithAttributes<A>).attributes))
        if (dirty) observers.forEach { it.update(entity) }
        dirty = true
    }
}
