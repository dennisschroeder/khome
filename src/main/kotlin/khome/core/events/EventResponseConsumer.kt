package khome.core.events

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.readText
import io.ktor.util.KtorExperimentalAPI
import khome.KhomeSession
import khome.core.HassEventResponse
import khome.core.MessageInterface
import khome.core.ResolverResponse
import khome.core.ResponseType
import khome.core.ResultResponse
import khome.core.StateChangedResponse
import khome.core.boot.BootSequenceInterface
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.entities.EntityIdToEntityTypeMap
import khome.core.entities.EntitySubject
import khome.core.mapping.ObjectMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging

@ExperimentalStdlibApi
@ObsoleteCoroutinesApi
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
internal class EventResponseConsumer(
    override val khomeSession: KhomeSession,
    private val objectMapper: ObjectMapper,
    private val hassEventRegistry: HassEventRegistry,
    private val errorResponseEvent: ErrorResponseEvent,
    private val entityIdToEntityTypeMap: EntityIdToEntityTypeMap
) : BootSequenceInterface, KhomeKoinComponent {
    private val logger = KotlinLogging.logger { }

    override suspend fun runBootSequence() = coroutineScope {
        khomeSession.consumeEachMappedToResponse { response, frameText ->
            when (response.type) {
                ResponseType.EVENT -> {
                    handleStateChangedResponse(frameText)
                    handleHassEventResponse(frameText)
                }
                ResponseType.RESULT -> {
                    handleSuccessResultResponse(frameText)
                    handleErrorResultResponse(frameText)
                }
            }
        }
    }

    private inline fun <reified Response : MessageInterface> mapFrameTextToResponse(frameText: Frame.Text): Response =
        objectMapper.fromJson(frameText.readText())

    private suspend inline fun WebSocketSession.consumeEachMappedToResponse(action: (ResolverResponse, Frame.Text) -> Unit) =
        incoming.consumeEach { frame ->
            (frame as? Frame.Text)?.let { frameText -> action(mapFrameTextToResponse(frameText), frameText) }
                ?: throw IllegalStateException("Frame could not ne casted to Frame.Text")
        }

    private fun handleStateChangedResponse(frameText: Frame.Text) =
        mapFrameTextToResponse<StateChangedResponse>(frameText)
            .takeIf { it.event.eventType == "state_changed" }
            ?.let { stateChangedResponse ->
                entityIdToEntityTypeMap[stateChangedResponse.event.data.entityId]?.let { clazz ->
                    logger.debug { "Found type for ${stateChangedResponse.event.data.entityId}" }
                    stateChangedResponse.event.data.newState?.let { state ->
                        logger.debug { "update entity state with $state" }
                        getKoin().get<EntitySubject<*>>(clazz, null, null)._state = state
                    }
                }
            }

    private suspend fun handleHassEventResponse(frameText: Frame.Text) =
        mapFrameTextToResponse<HassEventResponse>(frameText)
            .takeIf { hassEventResponse -> hassEventResponse.event.eventType != "state_changed" }
            ?.let { hassEventResponse -> emitHassEvent(hassEventRegistry, hassEventResponse) }

    private suspend fun emitHassEvent(registry: HassEventRegistry, hassEventResponse: HassEventResponse) =
        registry[hassEventResponse.event.eventType]?.let { event -> event.emit(hassEventResponse.event.data) }
            ?: logger.error {
                """
                    Somehow you subscribed to an event that is not registered in khomes
                    hass event registry and therefore khome could not emit the internal
                    event for this unknown event type: ${hassEventResponse.event.eventType}.
                """
            }

    private fun handleSuccessResultResponse(frameText: Frame.Text) =
        mapFrameTextToResponse<ResultResponse>(frameText)
            .takeIf { resultResponse -> resultResponse.success }
            ?.let { resultResponse -> logSuccessResult(resultResponse) }

    private suspend fun handleErrorResultResponse(frameText: Frame.Text) =
        mapFrameTextToResponse<ResultResponse>(frameText)
            .takeIf { resultResponse -> !resultResponse.success }
            ?.let { resultResponse ->
                logFailureResponse(resultResponse)
                emitFailureResponseEvent(resultResponse, errorResponseEvent)
            }

    private fun logSuccessResult(resultResponse: ResultResponse) =
        logger.info { "Result-Id: ${resultResponse.id} | Success: ${resultResponse.success}" }

    private fun logFailureResponse(resultResponse: ResultResponse) =
        logger.error { "CallId: ${resultResponse.id} -  errorCode: ${resultResponse.error!!.code} ${resultResponse.error.message}" }

    private suspend fun emitFailureResponseEvent(
        resultResponse: ResultResponse,
        errorResponseEvent: ErrorResponseEvent
    ) =
        errorResponseEvent.emit(resultResponse)
}
