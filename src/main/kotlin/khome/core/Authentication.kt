package khome.core

import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import khome.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import io.ktor.http.cio.websocket.WebSocketSession
import kotlin.system.exitProcess

@ObsoleteCoroutinesApi
internal suspend fun DefaultClientWebSocketSession.authenticate(configuration: ConfigurationInterface) {
    val initMessage = consumeMessage<AuthResponse>()

    if (initMessage.authRequired) {
        logger.info("Authentication required!")
        val authMessage = Auth(accessToken = configuration.accessToken).toJson()
        logger.info("Sending authentication message.")
        callWebSocketApi(authMessage)
    } else {
        logger.info("No authentication required!")
    }

    val authResponse =
        runCatching {
            incoming.receive().asObject<AuthResponse>()
        }

    authResponse.onFailure { logger.error { it.message } }
    authResponse.onSuccess {
        when {
            it.isAuthenticated -> logger.info { "Authenticated successfully." }
            it.authFailed -> {
                logger.error { "Authentication failed due to invalid credentials." }
                exitProcess(1)
            }
            else -> logger.error { "Something wen´t wrong. Cannot establish a connection." }
        }
    }
}

private data class Auth(
    val type: String = "auth",
    val accessToken: String
) : MessageInterface

private data class AuthResponse(
    val type: String,
    val haVersion: String
) : MessageInterface {
    val authRequired get() = type == "auth_required"
    val isAuthenticated get() = type == "auth_ok"
    val authFailed get() = type == "auth_invalid"
}
