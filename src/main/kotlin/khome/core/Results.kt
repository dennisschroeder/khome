package khome.core

import com.google.gson.annotations.SerializedName
import khome.core.exceptions.InvalidAttributeValueTypeException
import kotlinx.coroutines.delay
import java.time.OffsetDateTime
data class ResolverResponse(val id: Int, val type: ResponseType) : MessageInterface
data class StateChangedResponse(val id: Int, val type: ResponseType, val event: EventData) : MessageInterface
data class EventData(override val eventType: String, val data: Data, override val timeFired: OffsetDateTime, override val origin: String) :
    MessageInterface, EventDtoInterface

data class Data(val entityId: String, val oldState: State?, val newState: State?) : MessageInterface

data class HassEventResponse(val id: Int, val type: String, val event: HassEventData) : MessageInterface
data class HassEventData(val eventType: String, val data: Map<String, Any>, val timeFired: OffsetDateTime, val origin: String) :
    MessageInterface

interface EventDtoInterface {
    val eventType: String
    val timeFired: OffsetDateTime
    val origin: String
}

data class State(
    override val entityId: String,
    override val lastChanged: OffsetDateTime,
    override val state: Any,
    override val attributes: Map<String, Any>,
    override val lastUpdated: OffsetDateTime
) : StateInterface

interface StateInterface : MessageInterface {
    val entityId: String
    val lastChanged: OffsetDateTime
    val state: Any
    val attributes: Map<String, Any>
    val lastUpdated: OffsetDateTime
}

inline fun <reified T> StateInterface.getAttribute(key: String): T {
    return attributes[key] as? T ?: throw InvalidAttributeValueTypeException(
        "Attribute value for $key is of type: ${(attributes[key]
            ?: error("Key not valid"))::class}."
    )
}

suspend fun StateInterface.hasStateChangedAfter(millis: Long): Boolean {
    val initial = state
    delay(millis)
    val afterDelay = state
    return initial == afterDelay
}

suspend fun StateInterface.hasAttributesChangedAfter(millis: Long, vararg attributes: String): Boolean {
    val results = attributes.map { attribute ->
        hasAttributeChangedAfter(millis, attribute)
    }

    return results.contains(false)
}

suspend fun StateInterface.hasAttributeChangedAfter(millis: Long, attribute: String): Boolean {
    val initial = attributes[attribute]
    delay(millis)
    val afterDelay = attributes[attribute]
    return initial == afterDelay
}

suspend fun StateInterface.onlyIfStateHasNotChangedAfter(millis: Long, block: suspend () -> Unit) {
    if (hasStateChangedAfter(millis)) block()
}

suspend fun StateInterface.onlyIfAttributeHasNotChangedAfter(millis: Long, attribute: String, block: suspend () -> Unit) {
    if (hasAttributeChangedAfter(millis, attribute)) block()
}

data class ResultResponse(
    val id: Int,
    val type: String,
    val success: Boolean,
    val error: ErrorResult?,
    val result: Any?
) : MessageInterface

enum class ResponseType {
    @SerializedName("event")
    EVENT,
    @SerializedName("result")
    RESULT
}
