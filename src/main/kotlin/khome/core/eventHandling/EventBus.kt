package khome.core.eventHandling

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

typealias Handler<EventType> = Consumer<EventType>
internal operator fun <T> Handler<T>.invoke(t: T) = accept(t)
typealias EventIterator<T> = Iterable<MutableMap.MutableEntry<String, Handler<T>>>

open class Event<T> : EventIterator<T> {

    private val list = ConcurrentHashMap<String, Handler<T>>()

    private var nextUnnamedIndex = 0L

    val size: Int @JvmName("size") get() = list.size
    internal val listeners get() = list.entries

    internal fun clear() = list.clear()

    override operator fun iterator() = list.iterator()

    @JvmName("add")
    operator fun plusAssign(handler: Handler<T>) {
        list["${nextUnnamedIndex++}"] = handler
    }

    @JvmName("put")
    operator fun set(name: String, handler: Handler<T>) {
        list[name] = handler
    }

    @JvmName("add")
    inline operator fun plusAssign(crossinline handler: (T) -> Unit) {
        this += Handler { handler(it) }
    }

    @JvmName("put")
    inline operator fun set(name: String, crossinline handler: (T) -> Unit) {
        this[name] = Handler { handler(it) }
    }

    @JvmName("remove")
    operator fun minusAssign(name: String) {
        list.remove(name)
    }

    @JvmName("handle")
    operator fun invoke(data: T) {
        for ((_, value) in this) value(data)
    }
}
