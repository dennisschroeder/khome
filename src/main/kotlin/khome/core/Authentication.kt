package khome.core

import io.ktor.http.cio.websocket.WebSocketSession
import khome.*
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
suspend fun WebSocketSession.authenticate(token: String) {
    val initMessage = getMessage<AuthResponse>()

    if (initMessage.authRequired()) {
        Khome.logger.info("Authentication required!")
        val authMessage = Auth(accessToken = token).toJson()
        Khome.logger.info("Sending authentication message.")
        callWebSocketApi(authMessage)
    } else {
        Khome.logger.info("No authentication required!")
    }

    val authResponse = runCatching { incoming.receive().asObject<AuthResponse>() }
    authResponse.onFailure { Khome.logger.error { it.printStackTrace() } }
    authResponse.onSuccess { if (it.isAuthenticated()) Khome.logger.info { "Authenticated successfully." } }
}

data class Auth(
    override val type: String = "auth",
    val accessToken: String
) : Message

data class AuthResponse(
    override val type: String,
    val haVersion: String
) : Message

fun AuthResponse.authRequired() = type == "auth_required"
fun AuthResponse.isAuthenticated() = type == "auth_ok"