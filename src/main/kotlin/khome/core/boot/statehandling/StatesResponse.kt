package khome.core.boot.statehandling

import com.google.gson.JsonObject
import khome.core.MessageInterface

class StatesResponse(
    val id: Int,
    val type: String,
    val success: Boolean,
    val result: Array<JsonObject>
) : MessageInterface
