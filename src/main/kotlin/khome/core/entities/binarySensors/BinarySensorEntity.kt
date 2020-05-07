package khome.core.entities.binarySensors

import khome.core.entities.EntitySubject

@ExperimentalStdlibApi
abstract class BinarySensorEntity(name: String) : EntitySubject<String>("binary_sensor", name)
