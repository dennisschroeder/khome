package khome.core.entities.deviceTracker

import khome.calling.ServiceInterface
import khome.core.entities.AbstractEntity

abstract class AbstractDeviceTrackerEntity(name: String) :
    AbstractEntity<String>("device_tracker", name) {
    val isHome = stateValue == "home"
    val isAway = stateValue == "not_home"
    abstract val notifyIosService: ServiceInterface
}