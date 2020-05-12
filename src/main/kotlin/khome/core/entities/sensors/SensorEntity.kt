package khome.core.entities.sensors

import khome.core.entities.EntityId
import khome.core.entities.EntitySubject

abstract class SensorEntity(name: String) : EntitySubject<String>(EntityId("sensor", name))
