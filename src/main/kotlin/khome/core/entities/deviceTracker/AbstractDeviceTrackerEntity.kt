package khome.core.entities.deviceTracker

import khome.core.entities.AbstractEntity

abstract class AbstractDeviceTrackerEntity(name: String) :
    AbstractEntity<String>("device_tracker", name) {
    open val isHome get() = newState.state == "home"
    open val isAway get() = newState.state == "not_home"
}
