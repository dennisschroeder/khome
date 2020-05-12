package khome.core.entities.deviceTracker

import khome.core.entities.EntityId
import khome.core.entities.EntitySubject

abstract class DeviceTrackerEntity(name: String) :
    EntitySubject<String>(EntityId("device_tracker", name)) {
    open val isHome get() = stateValue == "home"
    open val isAway get() = stateValue == "not_home"
}
