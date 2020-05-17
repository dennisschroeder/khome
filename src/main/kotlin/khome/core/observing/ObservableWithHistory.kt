package khome.core.observing

interface ObservableWithHistoryInterface<T> {
    fun attachObserver(observerWithHistory: ObserverWithHistoryInterface<T>)
    fun getHistory(): List<T>
}

class ObservableWithHistory<T>(maxCapacity: Int = 10) : ObservableWithHistoryInterface<T> {
    var value: T
        get() = history.last() ?: throw IllegalStateException("No state available yet.")
        set(newValue) {
            val snapShot = history.snapshot()
            observers.forEach { it.update(snapShot, newValue) }
            history.add(newValue)
        }

    override fun attachObserver(observerWithHistory: ObserverWithHistoryInterface<T>) {
        observers.add(observerWithHistory)
    }

    private val observers: MutableList<ObserverWithHistoryInterface<T>> = mutableListOf()

    override fun getHistory(): List<T> = history.snapshot()
    private val history = CircularBuffer<T>(maxCapacity)
}
