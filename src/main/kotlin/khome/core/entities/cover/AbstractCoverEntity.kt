package khome.core.entities.cover

import khome.core.entities.AbstractEntity

abstract class AbstractCoverEntity(name: String) : AbstractEntity<String>("cover", name) {
    val isOpen get() = stateValue == "open"
    val isClosed get() = stateValue == "closed"
}
