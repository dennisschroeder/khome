package khome.core.boot.servicestore

import khome.core.MessageInterface

data class ServicesResponse(
    val id: Int,
    val type: String,
    val success: Boolean,
    val result: Map<String, Map<String, Any>>
) : MessageInterface
