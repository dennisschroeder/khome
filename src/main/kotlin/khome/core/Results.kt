package khome.core

import khome.core.exceptions.InvalidAttributeValueTypeException
import java.util.*
import khome.core.exceptions.InvalidStateValueTypeException

data class EventResult(val id: Int, override val type: String, val event: Event) :
    MessageInterface {
    data class Event(val eventType: String, val data: Data, val timeFired: Date, val origin: String) {
        data class Data(val entityId: String, val oldState: State, val newState: State)
    }
}

data class State(val entityId: String, val lastChanged: Date, val state: Any, val attributes: Map<String, Any>) {
    inline fun <reified T> getValue(): T {
        if (state !is T) throw InvalidStateValueTypeException("State value is of type: ${state.javaClass.kotlin.simpleName}.")

        return state
    }

    inline fun <reified T> getAttribute(key: String): T {
        return attributes[key] as? T ?: throw InvalidAttributeValueTypeException(
            "Attribute value for $key is of type: ${(attributes[key] ?: error("Key not valid")).javaClass.kotlin.simpleName}."
        )
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
