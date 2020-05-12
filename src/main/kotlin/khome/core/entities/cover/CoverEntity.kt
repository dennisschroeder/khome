package khome.core.entities.cover

import khome.core.entities.EntityId
import khome.core.entities.EntitySubject

abstract class CoverEntity(name: String) : EntitySubject<String>(EntityId("cover", name)) {
    open val isOpen get() = stateValue == "open"
    open val isClosed get() = stateValue == "closed"
}
