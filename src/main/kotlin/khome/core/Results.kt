package khome.core

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import khome.values.EntityId
import java.time.OffsetDateTime

internal data class ResolverResponse(val id: Int, val type: ResponseType) : MessageInterface
internal data class StateChangedResponse(val id: Int, val type: ResponseType, val event: StateChangedEventData) :
    MessageInterface

internal data class StateChangedEventData(
    override val eventType: String,
    val data: StateChangedData,
    override val timeFired: OffsetDateTime,
    override val origin: String
) : MessageInterface, EventDtoInterface

internal data class StateChangedData(val entityId: EntityId, val newState: JsonElement) :
    MessageInterface

interface EventDtoInterface {
    val eventType: String
    val timeFired: OffsetDateTime
    val origin: String
}

internal data class StateResponse(
    val entityId: EntityId,
    val lastChanged: OffsetDateTime,
    val state: Any,
    val attributes: JsonElement,
    val lastUpdated: OffsetDateTime
)

internal data class EventResponse(val id: Int, val type: ResponseType, val event: Event)
internal data class Event(
    override val eventType: String,
    val data: JsonElement,
    override val timeFired: OffsetDateTime,
    override val origin: String
) : MessageInterface, EventDtoInterface

internal data class ResultResponse(
    val id: Int,
    val type: String,
    val success: Boolean,
    val error: ErrorResponse?,
    val result: Any?
) : MessageInterface

internal enum class ResponseType {
    @SerializedName("event")
    EVENT,

    @SerializedName("result")
    RESULT
}
