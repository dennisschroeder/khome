package khome.core.observing

interface CircularBufferInterface<E> {
    fun add(e: E): Boolean
    fun last(): E?
    fun snapshot(): List<E>
}

class CircularBuffer<E>(private val maxCapacity: Int) : CircularBufferInterface<E> {
    private val backend: MutableList<E> = mutableListOf()
    override fun add(e: E) =
        backend.add(e).also {
            if (backend.size > maxCapacity) {
                backend.removeAt(backend.size - 1)
            }
        }

    override fun last() = backend.lastOrNull()
    override fun snapshot() = backend.toList()
}
