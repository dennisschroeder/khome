package khome.core.entities.deviceTracker

import khome.core.entities.AbstractEntity

abstract class AbstractDeviceTrackerEntity(name: String) :
    AbstractEntity<String>("device_tracker", name) {
    val isHome get() = stateValue == "home"
    val isAway get() = stateValue == "not_home"
}
