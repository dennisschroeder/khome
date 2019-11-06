package khome

import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.send

class KhomeSession(webSocketSession: DefaultClientWebSocketSession) : WebSocketSession by webSocketSession {
    suspend fun callWebsocketApi(message: String) = send(message)
}
