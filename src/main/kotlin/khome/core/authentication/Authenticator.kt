package khome.core.authentication

import khome.KhomeSession
import khome.core.ConfigurationInterface
import khome.core.boot.BootSequenceInterface
import mu.KotlinLogging

internal class Authenticator(
    override val khomeSession: KhomeSession,
    configuration: ConfigurationInterface
) : BootSequenceInterface {

    override suspend fun runBootSequence() =
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
    private val authRequest = AuthRequest(accessToken = configuration.accessToken)

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
