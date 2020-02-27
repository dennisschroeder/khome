package khome.core.clients

import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.ws
import io.ktor.client.features.websocket.wss
import io.ktor.http.HttpMethod

class WebSocketClient(delegate: HttpClient) {
    private val client = delegate

    suspend fun secureWebsocket(
        method: HttpMethod,
        host: String,
        port: Int,
        path: String,
        block: suspend DefaultClientWebSocketSession.() -> Unit
    ) =
        client.wss(
            method = method,
            host = host,
            port = port,
            path = path,
            block = block
        )

    suspend fun websocket(
        method: HttpMethod,
        host: String,
        port: Int,
        path: String,
        block: suspend DefaultClientWebSocketSession.() -> Unit
    ) =
        client.ws(
            method = method,
            host = host,
            port = port,
            path = path,
            block = block
        )
}
