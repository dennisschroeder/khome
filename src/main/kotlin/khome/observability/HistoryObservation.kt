package khome.observability

import khome.core.observing.CircularBuffer

internal class HistorySnapshot<T>(
    override val state: T,
    override val history: List<T>
) : WithHistory<T>

internal class ObservableHistoryNoInitial<T>(maxHistory: Int = 10) : ObservableHistory<T> {
    override var state: T
        get() = _history.last() ?: throw IllegalStateException("No value available yet.")
        set(newValue) {
            observers.forEach { it.update(HistorySnapshot(newValue, history)) }
            _history.add(newValue)
        }

    override val history: List<T>
        get() = _history.snapshot()

    private val _history = CircularBuffer<T>(maxHistory)

    @Suppress("UNCHECKED_CAST")
    override fun attachObserver(observer: Switchable) {
        observers.add(observer as SwitchableObserver<T>)
    }

    private val observers = mutableListOf<SwitchableObserver<T>>()
}
