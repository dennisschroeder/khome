package khome.core

import com.google.gson.annotations.SerializedName
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

data class NewState(private val delegate: State) : StateInterface by delegate
data class OldState(private val delegate: State) : StateInterface by delegate

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
