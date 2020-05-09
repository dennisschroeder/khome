package khome.core.entities.binarySensors

import khome.core.entities.EntitySubject

abstract class BinarySensorEntity(name: String) : EntitySubject<String>("binary_sensor", name)
