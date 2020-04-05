package khome.core.entities.cover

import khome.core.entities.AbstractEntity

abstract class AbstractCoverEntity(name: String) : AbstractEntity<String>("cover", name) {
    val isOpen get() = newState.state == "open"
    val isClosed get() = newState.state == "closed"
}
