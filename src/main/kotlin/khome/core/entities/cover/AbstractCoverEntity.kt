package khome.core.entities.cover

import khome.core.entities.AbstractEntity

abstract class AbstractCoverEntity(name: String) : AbstractEntity("cover", name) {
    val isOpen = getStateValue<String>() == "open"
    val isClosed = getStateValue<String>() == "closed"
}