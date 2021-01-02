package khome

import io.ktor.client.features.websocket.ClientWebSocketSession
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import io.ktor.util.KtorExperimentalAPI
import khome.core.MessageInterface
import khome.core.mapping.ObjectMapperInterface
import khome.core.mapping.fromJson
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
internal class KhomeSession(
    delegate: DefaultClientWebSocketSession,
    val objectMapper: ObjectMapperInterface
) : ClientWebSocketSession by delegate {

    private val logger = KotlinLogging.logger {}
    suspend fun callWebSocketApi(message: String) =
        send(message).also { logger.debug { "Called hass api with message: $message" } }

    suspend fun callWebSocketApi(message: MessageInterface) =
        send(message.toJson()).also { logger.debug { "Called hass api with message: ${message.toJson()}" } }

    suspend inline fun <reified M : Any> consumeSingleMessage(): M = incoming.receive().asObject()
    inline fun <reified M : Any> Frame.asObject() = (this as Frame.Text).toObject<M>()
    inline fun <reified M : Any> Frame.Text.toObject(): M = objectMapper.fromJson(readText())

    private fun MessageInterface.toJson(): String = objectMapper.toJson(this)
}
