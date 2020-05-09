package khome.core.entities.deviceTracker

import khome.core.entities.EntitySubject

abstract class DeviceTrackerEntity(name: String) :
    EntitySubject<String>("device_tracker", name) {
    open val isHome get() = state == "home"
    open val isAway get() = state == "not_home"
}
