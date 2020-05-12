package khome.core.entities.binarySensors

import khome.core.entities.EntityId
import khome.core.entities.EntitySubject

abstract class BinarySensorEntity(name: String) : EntitySubject<String>(EntityId("binary_sensor", name))
