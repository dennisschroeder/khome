package khome.core.boot

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.readText
import io.ktor.util.KtorExperimentalAPI
import khome.EventHandlerByEventType
import khome.KhomeSession
import khome.core.EventResponse
import khome.core.ResolverResponse
import khome.core.ResponseType
import khome.core.ResultResponse
import khome.core.StateChangedResponse
import khome.core.boot.statehandling.flattenStateAttributes
import khome.core.mapping.ObjectMapperInterface
import khome.core.mapping.fromJson
import khome.entities.ActuatorStateUpdater
import khome.entities.SensorStateUpdater
import khome.errorHandling.ErrorResponseData
import khome.errorHandling.ErrorResponseHandlerImpl
import khome.values.EventType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging

interface EventResponseConsumer {
    suspend fun consumeBlocking()
}

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
internal class EventResponseConsumerImpl(
    private val khomeSession: KhomeSession,
    private val objectMapper: ObjectMapperInterface,
    private val sensorStateUpdater: SensorStateUpdater,
    private val actuatorStateUpdater: ActuatorStateUpdater,
    private val eventHandlerByEventType: EventHandlerByEventType,
    private val errorResponseHandler: (ErrorResponseData) -> Unit
) : EventResponseConsumer {
    private val logger = KotlinLogging.logger { }

    @ExperimentalStdlibApi
    override suspend fun consumeBlocking() = coroutineScope {
        khomeSession.consumeEachMappedToResponse { response, frameText ->
            when (response.type) {
                ResponseType.EVENT -> {
                    handleStateChangedResponse(frameText)
                    handleEventResponse(frameText)
                }
                ResponseType.RESULT -> {
                    handleSuccessResultResponse(frameText)
                    handleErrorResultResponse(frameText)
                }
            }
        }
    }

    private inline fun <reified Response> mapFrameTextToResponse(frameText: Frame.Text): Response =
        objectMapper.fromJson(frameText.readText())

    private suspend inline fun WebSocketSession.consumeEachMappedToResponse(action: (ResolverResponse, Frame.Text) -> Unit) =
        incoming.consumeEach { frame ->
            (frame as? Frame.Text)?.let { frameText -> action(mapFrameTextToResponse(frameText), frameText) }
                ?: throw IllegalStateException("Frame could not ne casted to Frame.Text")
        }

    @ExperimentalStdlibApi
    private fun handleStateChangedResponse(frameText: Frame.Text) =
        mapFrameTextToResponse<StateChangedResponse>(frameText)
            .takeIf { it.event.eventType == "state_changed" }
            ?.let { stateChangedResponse ->
                logger.debug { "State change response: $stateChangedResponse" }
                stateChangedResponse.event.data.newState.getOrNull()?.let { newState ->
                    sensorStateUpdater(
                        flattenStateAttributes(newState.asJsonObject),
                        stateChangedResponse.event.data.entityId
                    )
                    actuatorStateUpdater(
                        flattenStateAttributes(newState.asJsonObject),
                        stateChangedResponse.event.data.entityId
                    )
                }
            }

    private fun handleEventResponse(frameText: Frame.Text) {
        mapFrameTextToResponse<EventResponse>(frameText)
            .takeIf { EventType.from(it.event.eventType) in eventHandlerByEventType }
            ?.let { eventResponse ->
                logger.debug { "Event response: $eventResponse" }
                eventHandlerByEventType[EventType.from(eventResponse.event.eventType)]
                    ?.invokeEventHandler(eventResponse.event.data)
                    ?: logger.warn { "No event found for event type: ${eventResponse.event.eventType}" }
            }
    }

    private fun handleSuccessResultResponse(frameText: Frame.Text) =
        mapFrameTextToResponse<ResultResponse>(frameText)
            .takeIf { resultResponse -> resultResponse.success }
            ?.let { resultResponse -> logSuccessResult(resultResponse) }

    private fun handleErrorResultResponse(frameText: Frame.Text) =
        mapFrameTextToResponse<ResultResponse>(frameText)
            .takeIf { resultResponse -> !resultResponse.success }
            ?.let { resultResponse ->
                ErrorResponseHandlerImpl(errorResponseHandler).handle(
                    ErrorResponseData(
                        commandId = resultResponse.id,
                        errorResponse = resultResponse.error!!
                    )
                )
            }

    private fun logSuccessResult(resultResponse: ResultResponse) =
        logger.info { "Result-Id: ${resultResponse.id} | Success: ${resultResponse.success}" }
}

private fun JsonElement.getOrNull(): JsonElement? = if (this is JsonNull) null else this
