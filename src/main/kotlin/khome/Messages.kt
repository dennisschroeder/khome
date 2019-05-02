package khome

import khome.Khome.Companion.serializer
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import java.util.*
import kotlin.reflect.KClass


data class ListenEvent(
    val id: Int,
    override val type: String = "subscribe_events",
    val eventType: String
) : Message

data class EventResult(val id: Int, override val type: String, val event: Event) :
    Message {
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

inline fun <reified M : Any> Frame.Text.toObject(): M = toObject(M::class)

fun <M : Any> Frame.Text.toObject(type: KClass<M>): M {
    val gson = serializer
    return gson.fromJson<M>(readText(), type.java)
}

data class FetchStates(val id: Int, override val type: String = "get_states") : Message

data class Result(
    val id: Int,
    override val type: String,
    val success: Boolean,
    val error: Map<String, String>?,
    val result: Map<String, String>?
) : Message

data class StateResult(
    val id: Int,
    override val type: String,
    val success: Boolean,
    val result: Array<State>
) : Message {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StateResult

        if (id != other.id) return false
        if (type != other.type) return false
        if (success != other.success) return false
        if (!result.contentEquals(other.result)) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = id
        result1 = 31 * result1 + type.hashCode()
        result1 = 31 * result1 + success.hashCode()
        result1 = 31 * result1 + result.contentHashCode()
        return result1
    }
}


interface Message {
    val type: String
}

fun Message.toJson() = serializer.toJson(this)
