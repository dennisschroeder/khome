package khome.core

import java.util.*

data class EventResult(val id: Int, override val type: String, val event: Event) :
    MessageInterface {
    data class Event(val eventType: String, val data: Data, val timeFired: Date, val origin: String) {
        data class Data(val entityId: String, val oldState: State, val newState: State)
    }
}

data class State(val entityId: String, val lastChanged: Date, val state: Any, val attributes: Map<String, Any>) {
    inline fun <reified T> get(): T? {
        if (state !is T) return null

        return state
    }

    inline fun <reified T> getAttribute(key: String): T? {
        return attributes[key] as? T ?: return null
    }
}

data class Result(
    val id: Int,
    override val type: String,
    val success: Boolean,
    val error: Map<String, String>?,
    val result: Map<String, String>?
) : MessageInterface

data class StateResult(
    val id: Int,
    override val type: String,
    val success: Boolean,
    val result: Array<State>
) : MessageInterface
