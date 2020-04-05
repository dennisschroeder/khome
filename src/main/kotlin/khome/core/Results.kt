package khome.core

import khome.core.eventHandling.EventData
import java.time.OffsetDateTime

data class EventResult(val id: Int, val type: String, val event: Event) : MessageInterface
data class Event(override val eventType: String, val data: Data, override val timeFired: OffsetDateTime, override val origin: String) :
    MessageInterface, EventDtoInterface

data class Data(val entityId: String, val oldState: State?, val newState: State?) : MessageInterface
data class HassEventResultDto(val id: Int, val type: String, val event: HassEventDto) : MessageInterface
data class HassEventDto(val eventType: String, val data: EventData, val timeFired: OffsetDateTime, val origin: String) :
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

data class Result(
    val id: Int,
    val type: String,
    val success: Boolean,
    val error: ErrorResult?,
    val result: Any?
) : MessageInterface

class StateResult(
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
