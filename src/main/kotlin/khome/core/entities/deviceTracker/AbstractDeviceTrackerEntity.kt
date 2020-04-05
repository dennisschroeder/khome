package khome.core.entities.deviceTracker

import khome.core.entities.AbstractEntity

abstract class AbstractDeviceTrackerEntity(name: String) :
    AbstractEntity<String>("device_tracker", name) {
    val isHome get() = newState.state == "home"
    val isAway get() = newState.state == "not_home"
}
