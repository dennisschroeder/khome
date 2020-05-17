package khome.core.boot

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.readText
import io.ktor.util.KtorExperimentalAPI
import khome.ActuatorsByApiName
import khome.KhomeSession
import khome.SensorsByApiName
import khome.core.MessageInterface
import khome.core.ResolverResponse
import khome.core.ResponseType
import khome.core.ResultResponse
import khome.core.StateChangedResponse
import khome.core.mapping.ObjectMapper
import khome.entities.ActuatorStateUpdater
import khome.entities.SensorStateUpdater
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
internal class EventResponseConsumer(
    override val khomeSession: KhomeSession,
    private val objectMapper: ObjectMapper,
    private val sensorStateUpdater: SensorStateUpdater,
    private val actuatorStateUpdater: ActuatorStateUpdater
) : BootSequenceInterface {
    private val logger = KotlinLogging.logger { }

    @ExperimentalStdlibApi
    override suspend fun runBootSequence() = coroutineScope {
        khomeSession.consumeEachMappedToResponse { response, frameText ->
            when (response.type) {
                ResponseType.EVENT -> {
                    handleStateChangedResponse(frameText)
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

    @ExperimentalStdlibApi
    private fun handleStateChangedResponse(frameText: Frame.Text) =
        mapFrameTextToResponse<StateChangedResponse>(frameText)
            .takeIf { it.event.eventType == "state_changed" }
            ?.let { stateChangedResponse ->
                stateChangedResponse.event.data.newState?.let { newState ->
                    sensorStateUpdater(newState)
                    actuatorStateUpdater(newState)
                }
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
            }

    private fun logSuccessResult(resultResponse: ResultResponse) =
        logger.info { "Result-Id: ${resultResponse.id} | Success: ${resultResponse.success}" }

    private fun logFailureResponse(resultResponse: ResultResponse) =
        logger.error { "CallId: ${resultResponse.id} -  errorCode: ${resultResponse.error!!.code} ${resultResponse.error.message}" }
}
