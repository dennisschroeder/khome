package khome.calling

import khome.core.MessageInterface

internal data class FetchServices(val id: Int, val type: String = "get_services") : MessageInterface