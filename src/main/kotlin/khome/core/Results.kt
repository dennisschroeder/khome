package khome.core

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import khome.entities.EntityId
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

/*

 State change response:
    StateChangedResponse(
        id=3,
        type=EVENT,
        event=StateChangedEventData(
            eventType=state_changed,
            data=StateChangedData(
                entityId=light.decoration_downstairs,
                newState={
                    "entity_id":"light.decoration_downstairs",
                    "state":"on",
                    "attributes":{
                        "friendly_name":"decoration_downstairs",
                        "supported_features":0
                     },
                    "last_changed":"2020-07-11T15:50:53.288535+00:00",
                    "last_updated":"2020-07-11T15:50:53.288535+00:00",
                    "context":{
                        "id":"1f31710a4739448e8753ced2a0c984a2",
                        "parent_id":null,
                        "user_id":"857af942982e4c4a8fa991edd0974466"
                     }
                 }
             ),
             timeFired=2020-07-11T15:50:53.288558Z,
             origin=LOCAL)
   )

 */

/*

 State change response:
    StateChangedResponse(
        id=3,
        type=EVENT,
        event=StateChangedEventData(
            eventType=state_changed,
            data=StateChangedData(
                entityId=light.decoration_downstairs,
                newState={
                    "entity_id":"light.decoration_downstairs",
                    "state":"on",
                    "attributes":{
                        "friendly_name":"decoration_downstairs",
                        "supported_features":0
                     },
                    "last_changed":"2020-07-11T15:50:53.288535+00:00",
                    "last_updated":"2020-07-11T15:50:53.288535+00:00",
                    "context":{
                        "id":"1f31710a4739448e8753ced2a0c984a2",
                        "parent_id":null,
                        "user_id":"857af942982e4c4a8fa991edd0974466"
                     }
                 }
             ),
             timeFired=2020-07-11T15:50:53.288558Z,
             origin=LOCAL)
   )

 */
