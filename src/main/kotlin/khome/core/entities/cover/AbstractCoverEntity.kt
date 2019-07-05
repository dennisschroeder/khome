package khome.core.entities.cover

import khome.core.entities.AbstractEntity

abstract class AbstractCoverEntity(name: String) : AbstractEntity<String>("cover", name) {
    val isOpen = stateValue == "open"
    val isClosed = stateValue == "closed"
}