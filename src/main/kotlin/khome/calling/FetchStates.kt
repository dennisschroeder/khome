package khome.calling

import khome.core.MessageInterface

internal data class FetchStates(val id: Int, val type: String = "get_states") : MessageInterface