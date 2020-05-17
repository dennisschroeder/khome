package khome.core.observing

interface ObserverWithHistoryInterface<T> {
    fun update(history: List<T>, value: T)
    fun enable()
    fun disable()
}
