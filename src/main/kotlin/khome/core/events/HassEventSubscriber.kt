package khome.core.events

import khome.KhomeSession
import khome.core.ResultResponse
import khome.core.boot.BootSequenceInterface
import khome.core.dependencyInjection.CallerID
import mu.KotlinLogging

internal class HassEventSubscriber(
    override val khomeSession: KhomeSession,
    private val callerID: CallerID,
    private val registry: HassEventRegistry
) : BootSequenceInterface {
    private val logger = KotlinLogging.logger { }
    private val id
        get() = callerID.incrementAndGet()

    override suspend fun runBootSequence() {
        registry.forEach { eventType ->
            EventListeningRequest(id = id, eventType = eventType.key).run {
                sendEventListenerRequest(this)
                consumeResultResponse().let { resultResponse ->
                    when (resultResponse.success) {
                        false -> logger.error { "could not register event ${eventType.key}" }
                        true -> logger.info { "CallerId: $id - Subscribed to custom event: ${eventType.key}" }
                    }
                }
            }
        }
    }

    private suspend fun sendEventListenerRequest(request: EventListeningRequest) =
        khomeSession.callWebSocketApi(request)

    private suspend fun consumeResultResponse() =
        khomeSession.consumeSingleMessage<ResultResponse>()
}
