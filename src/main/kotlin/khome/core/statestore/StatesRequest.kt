package khome.core.statestore

import khome.core.MessageInterface

internal data class StatesRequest(val id: Int, val type: String = "get_states") :
    MessageInterface
