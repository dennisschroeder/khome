package khome.core.events

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

typealias Handler<EventDataType> = suspend CoroutineScope.(EventDataType) -> Unit
typealias EventIterator<EventDataType> = Iterable<MutableMap.MutableEntry<String, Handler<EventDataType>>>

class Event<EventDataType>(private val exceptionHandler: EventListenerExceptionHandler) : EventIterator<EventDataType> {
    private val scope
        get() = CoroutineScope(Dispatchers.IO)
    private val list = ConcurrentHashMap<String, Handler<EventDataType>>()
    private var nextUnnamedIndex = 0L

    val size: Int get() = list.size
    internal val listeners
        get() = list.entries

    internal fun clear() =
        list.clear()

    override operator fun iterator() =
        list.iterator()

    operator fun plusAssign(handler: Handler<EventDataType>) {
        list["${nextUnnamedIndex++}"] = handler
    }

    operator fun set(name: String, handler: Handler<EventDataType>) {
        list[name] = handler
    }

    operator fun minusAssign(name: String) {
        list.remove(name)
    }

    operator fun invoke(data: EventDataType) {
        for ((_, value) in this)
            scope.launch(exceptionHandler) { value(data) }
    }
}
