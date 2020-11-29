package khome.testing

import io.ktor.client.statement.HttpResponse
import khome.communicating.HassApiClient
import khome.communicating.HassApiCommand
import khome.core.mapping.ObjectMapper
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import mu.KotlinLogging

internal class HassApiTestClient(
    private val mapper: ObjectMapper
) : HassApiClient {
    private val logger = KotlinLogging.logger { }

    override fun sendCommand(command: HassApiCommand): Job {
        logger.info { "SANDBOX MODE ACTIVE" }
        mapper.toJson(command).let { serializedCommand ->
            logger.info { "Would have called hass api with message: $serializedCommand" }
        }
        return Job()
    }

    override fun emitEvent(eventType: String, eventData: Any?) {
        TODO("Not yet implemented")
    }

    override fun emitEventAsync(eventType: String, eventData: Any?): Deferred<HttpResponse> {
        TODO("Not yet implemented")
    }
}
