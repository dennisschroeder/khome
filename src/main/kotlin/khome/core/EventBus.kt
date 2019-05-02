package khome.core
import java.util.function.Consumer

typealias Handler<EventType> = Consumer<EventType>

internal operator fun <T> Handler<T>.invoke(t: T) = accept(t)

class Event<T> : Iterable<MutableMap.MutableEntry<String, Handler<T>>> {

    private val list = LinkedHashMap<String, Handler<T>>()

    var nextUnnamedIndex = 0L
        private set

    val size: Int @JvmName("size") get() = list.size
    val listeners: MutableCollection<MutableMap.MutableEntry<String, Handler<T>>> get() = list.entries

    fun clear() = list.clear()

    override operator fun iterator() = list.iterator()

    @JvmName("add")
    operator fun plusAssign(handler: Handler<T>) {
        list.put("${nextUnnamedIndex++}", handler)
    }

    @JvmName("put")
    operator fun set(name: String, handler: Handler<T>) {
        list.put(name, handler)
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