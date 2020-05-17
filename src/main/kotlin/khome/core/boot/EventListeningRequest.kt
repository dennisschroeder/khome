package khome.core.boot

import khome.core.MessageInterface

data class EventListeningRequest(
    val id: Int,
    val type: String = "subscribe_events",
    val eventType: String
) : MessageInterface
