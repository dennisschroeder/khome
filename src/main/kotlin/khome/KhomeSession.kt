package khome

import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.send
import io.ktor.util.KtorExperimentalAPI
import khome.core.dependencyInjection.KhomeKoinComponent
import kotlinx.coroutines.ObsoleteCoroutinesApi

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
class KhomeSession(webSocketSession: DefaultClientWebSocketSession) : KhomeKoinComponent(), WebSocketSession by webSocketSession {
    suspend fun callWebsocketApi(message: String) = send(message)
    internal suspend inline fun <reified M : Any> consumeMessage(): M =
        incoming.receive().asObject()
}
