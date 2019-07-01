package khome.core

import khome.*
import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
suspend fun WebSocketSession.authenticate(token: String) {
    val initMessage = getMessage<AuthResponse>()

    if (initMessage.authRequired) {
        logger.info("Authentication required!")
        val authMessage = Auth(accessToken = token).toJson()
        logger.info("Sending authentication message.")
        callWebSocketApi(authMessage)
    } else {
        logger.info("No authentication required!")
    }

    val authResponse = runCatching { incoming.receive().asObject<AuthResponse>() }
    authResponse.onFailure { logger.error { it.printStackTrace() } }
    authResponse.onSuccess { if (it.isAuthenticated) logger.info { "Authenticated successfully." } }
}

data class Auth(
    override val type: String = "auth",
    val accessToken: String
) : MessageInterface

data class AuthResponse(
    override val type: String,
    val haVersion: String
) : MessageInterface {
    val authRequired get() = type == "auth_required"
    val isAuthenticated get() = type == "auth_ok"
}