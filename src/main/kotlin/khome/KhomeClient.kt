package khome

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.client.features.websocket.wss
import io.ktor.http.HttpMethod
import io.ktor.util.KtorExperimentalAPI
import khome.core.ConfigurationInterface

@KtorExperimentalAPI
class KhomeClient(private val config: ConfigurationInterface) {

    private val httpClient = HttpClient(CIO).config { install(WebSockets) }

    private val method = HttpMethod.Get
    private val path = "/api/websocket"
    private val isSecure: Boolean = config.secure

    suspend fun startSession(block: suspend DefaultClientWebSocketSession.() -> Unit) = when (isSecure) {
        true -> httpClient.wss(method = method, host = config.host, port = config.port, path = path, block = block)
        false -> httpClient.ws(method = method, host = config.host, port = config.port, path = path, block = block)
    }
}
