package khome.core.statestore

import khome.core.MessageInterface
import khome.core.StateResponse

class StatesResponse(
    val id: Int,
    val type: String,
    val success: Boolean,
    val result: Array<StateResponse>
) : MessageInterface
