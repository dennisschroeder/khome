package khome.core.boot.servicestore

import khome.core.MessageInterface

internal data class ServicesRequest(val id: Int, val type: String = "get_services") :
    MessageInterface
