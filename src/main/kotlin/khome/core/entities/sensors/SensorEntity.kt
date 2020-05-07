package khome.core.entities.sensors

import khome.core.entities.EntitySubject

@ExperimentalStdlibApi
abstract class SensorEntity(name: String) : EntitySubject<String>("sensor", name)
