package khome.core

data class ListenEvent(
    val id: Int,
    override val type: String = "subscribe_events",
    val eventType: String
) : MessageInterface