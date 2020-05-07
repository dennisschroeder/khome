package khome.core.entities.deviceTracker

import khome.core.entities.EntitySubject

@ExperimentalStdlibApi
abstract class DeviceTrackerEntity(name: String) :
    EntitySubject<String>("device_tracker", name) {
    open val isHome get() = state.state == "home"
    open val isAway get() = state.state == "not_home"
}
