package khome.core.boot.authentication

import khome.core.MessageInterface

internal data class AuthRequest(
    val type: String = "auth",
    val accessToken: String
) : MessageInterface
