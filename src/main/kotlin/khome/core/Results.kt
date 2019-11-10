package khome.core

import khome.core.exceptions.InvalidAttributeValueTypeException
import khome.core.exceptions.InvalidStateValueTypeException
import java.util.Date

data class EventResult(val id: Int, val type: String, val event: Event) :
    MessageInterface {
    data class Event(val eventType: String, val data: Data, val timeFired: Date, val origin: String) {
        data class Data(val entityId: String, val oldState: State?, val newState: State?)
    }
}

data class State(
    override val entityId: String,
    override val lastChanged: Date,
    override val state: Any,
    override val attributes: Map<String, Any>
) :
    StateInterface {
    inline fun <reified T> getValue(): T {
        if (state !is T) throw InvalidStateValueTypeException("State value is of type: ${state::class}.")
        return state
    }

    inline fun <reified T> getAttribute(key: String): T {
        return attributes[key] as? T ?: throw InvalidAttributeValueTypeException(
            "Attribute value for $key is of type: ${(attributes[key]
                ?: error("Key not valid"))::class}."
        )
    }
}

interface StateInterface : MessageInterface {
    val entityId: String
    val lastChanged: Date
    val state: Any
    val attributes: Map<String, Any>
}

data class Result(
    val id: Int,
    val type: String,
    val success: Boolean,
    val error: Map<String, String>?,
    val result: Any
) : MessageInterface

data class StateResult(
    val id: Int,
    val type: String,
    val success: Boolean,
    val result: Array<State>
) : MessageInterface

data class ServiceResult(
    val id: Int,
    val type: String,
    val success: Boolean,
    val result: Map<String, Map<String, Any>>
) : MessageInterface
