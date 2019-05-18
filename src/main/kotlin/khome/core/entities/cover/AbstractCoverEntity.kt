package khome.core.entities.cover

import khome.core.entities.AbstractEntity

abstract class AbstractCoverEntity(coverEntityName: String) : AbstractEntity("cover", coverEntityName) {
    val isOpen = getStateValue<String>() == "open"
    val isClosed = getStateValue<String>() == "closed"
}