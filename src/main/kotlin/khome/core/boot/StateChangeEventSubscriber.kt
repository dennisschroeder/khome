package khome.core.boot

import khome.KhomeSession
import khome.core.ResultResponse
import khome.communicating.CALLER_ID
import mu.KotlinLogging

internal class StateChangeEventSubscriber(
    override val khomeSession: KhomeSession
) : StartSequenceStep {

    private val logger = KotlinLogging.logger { }
    private val id
        get() = CALLER_ID.incrementAndGet()

    override suspend fun runStartSequenceStep() {
        sendEventListenerRequest()
        consumeResultResponse().let { resultResponse ->
            when (resultResponse.success) {
                false -> logger.error { "Could not subscribe to state change events" }
                true -> logger.info { "Successfully started listening to state changes" }
            }
        }
    }

    private val eventListenerRequest =
        EventListeningRequest(id = id, eventType = "state_changed")

    private suspend fun sendEventListenerRequest() =
        khomeSession.callWebSocketApi(eventListenerRequest)

    private suspend fun consumeResultResponse() =
        khomeSession.consumeSingleMessage<ResultResponse>()
}
