package khome.core.boot.statehandling

import khome.core.MessageInterface

internal data class StatesRequest(val id: Int, val type: String = "get_states") :
    MessageInterface
