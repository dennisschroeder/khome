package khome.core.boot

import khome.KhomeSession
import khome.core.ResultResponse
import khome.core.koin.CallerID
import mu.KotlinLogging

internal class StateChangeEventSubscriber(
    override val khomeSession: KhomeSession,
    private val callerID: CallerID
) : BootSequenceInterface {

    private val logger = KotlinLogging.logger { }
    private val id
        get() = callerID.incrementAndGet()

    override suspend fun runBootSequence() {
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
