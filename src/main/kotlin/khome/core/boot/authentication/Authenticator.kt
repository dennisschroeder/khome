package khome.core.boot.authentication

import khome.KhomeSession
import khome.core.Configuration
import mu.KotlinLogging

internal class AuthenticatorImpl(
    private val khomeSession: KhomeSession,
    configuration: Configuration
) : Authenticator {

    override suspend fun authenticate() =
        consumeInitialResponse()
            .let { initialResponse ->
                when (initialResponse.type) {
                    "auth_required" -> {
                        logger.info("Authentication required!")
                        sendAuthenticationMessage()
                        consumeAuthenticationResponse()
                            .let { authResponse ->
                                when (authResponse.type) {
                                    "auth_ok" -> logger.info { "Authenticated successfully to homeassistant version ${authResponse.haVersion}" }
                                    "auth_invalid" -> logger.error { "Authentication failed. Server send: ${authResponse.message}" }
                                }
                            }
                    }
                    "auth_ok" -> logger.info { "Authenticated successfully (no authentication needed)." }
                }
            }

    private val logger = KotlinLogging.logger { }
    private val authRequest =
        AuthRequest(accessToken = configuration.accessToken)

    private suspend fun consumeInitialResponse() =
        khomeSession.consumeSingleMessage<InitialResponse>()

    private suspend fun consumeAuthenticationResponse() =
        khomeSession.consumeSingleMessage<AuthResponse>()

    private suspend fun sendAuthenticationMessage() =
        try {
            khomeSession.callWebSocketApi(authRequest).also { logger.info("Sending authentication message.") }
        } catch (e: Exception) {
            logger.error(e) { "Could not send authentication message" }
        }
}

interface Authenticator {
    suspend fun authenticate()
}
