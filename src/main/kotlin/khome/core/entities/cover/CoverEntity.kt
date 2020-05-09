package khome.core.entities.cover

import khome.core.entities.EntitySubject

abstract class CoverEntity(name: String) : EntitySubject<String>("cover", name) {
    open val isOpen get() = state == "open"
    open val isClosed get() = state == "closed"
}
