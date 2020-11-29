package khome.core.boot.subscribing

import khome.EventHandlerByEventType
import khome.KhomeSession
import khome.communicating.HassApiClient
import khome.communicating.SubscribeEventCommand
import khome.core.ResultResponse
import mu.KotlinLogging

interface HassEventSubscriber {
    suspend fun subscribe()
}

internal class HassEventSubscriberImpl(
    private val khomeSession: KhomeSession,
    private val subscriptions: EventHandlerByEventType,
    private val hassApi: HassApiClient
) : HassEventSubscriber {
    private val logger = KotlinLogging.logger { }

    override suspend fun subscribe() {
        subscriptions.forEach { entry ->
            SubscribeEventCommand(entry.key).also { command -> hassApi.sendCommand(command) }
            khomeSession.consumeSingleMessage<ResultResponse>()
                .takeIf { resultResponse -> resultResponse.success }
                ?.let { logger.info { "Subscribed to event: ${entry.key}" } }
        }
    }
}
