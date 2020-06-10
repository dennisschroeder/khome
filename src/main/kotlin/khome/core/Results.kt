package khome.core

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import khome.entities.EntityId
import java.time.Instant
import java.time.OffsetDateTime

data class ResolverResponse(val id: Int, val type: ResponseType) : MessageInterface
data class StateChangedResponse(val id: Int, val type: ResponseType, val event: StateChangedEventData) : MessageInterface
data class StateChangedEventData(
    override val eventType: String,
    val data: StateChangedData,
    override val timeFired: OffsetDateTime,
    override val origin: String
) : MessageInterface, EventDtoInterface

data class StateChangedData(val entityId: EntityId, val oldState: JsonElement?, val newState: JsonElement?) : MessageInterface

interface EventDtoInterface {
    val eventType: String
    val timeFired: OffsetDateTime
    val origin: String
}

data class StateResponse(
    val entityId: EntityId,
    val lastChanged: OffsetDateTime,
    val state: Any,
    val attributes: JsonElement,
    val lastUpdated: OffsetDateTime
)

interface Attributes {
    val lastChanged: Instant
    val lastUpdated: Instant
    val friendlyName: String
}

interface State<T> {
    val value: T
}

data class EventResponse(val id: Int, val type: ResponseType, val event: Event)
data class Event(
    override val eventType: String,
    val data: JsonElement,
    override val timeFired: OffsetDateTime,
    override val origin: String
) : MessageInterface, EventDtoInterface

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
