package khome.core.observing

import java.util.LinkedList

interface CircularBufferInterface<E> {
    fun addFirst(e: E)
    val last: E?
    val first: E?
    val snapshot: List<E>
}

class CircularBuffer<E>(private val maxCapacity: Int) : CircularBufferInterface<E> {
    private val backend: LinkedList<E> = LinkedList()
    override val snapshot: List<E>
        get() = backend.toList()

    override val first: E?
        get() = backend.firstOrNull()

    override val last: E?
        get() = backend.lastOrNull()

    override fun addFirst(e: E) =
        backend.addFirst(e).also {
            if (backend.size > maxCapacity) {
                backend.removeAt(backend.size - 1)
            }
        }
}
