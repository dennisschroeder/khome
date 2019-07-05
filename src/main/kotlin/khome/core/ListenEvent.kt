package khome.core

data class ListenEvent(
    val id: Int,
    val type: String = "subscribe_events",
    val eventType: String
) : MessageInterface