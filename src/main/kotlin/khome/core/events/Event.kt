package khome.core.events

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.FlowCollector
import java.util.concurrent.ConcurrentHashMap

typealias Handler<EventDataType> = (EventDataType) -> Unit
typealias EventIterator<EventDataType> = Iterable<MutableMap.MutableEntry<String, Handler<EventDataType>>>

class Event<EventDataType> : EventIterator<EventDataType>, AbstractFlow<Handler<EventDataType>>() {
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

    operator fun get(name: String) =
        list[name]

    operator fun minusAssign(name: String) {
        list.remove(name)
    }

    @FlowPreview
    override suspend fun collectSafely(collector: FlowCollector<Handler<EventDataType>>) {
        for ((_, value) in this) collector.emit(value)
    }
}
