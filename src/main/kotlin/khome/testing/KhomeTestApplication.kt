package khome.testing

import khome.entities.devices.Actuator

interface KhomeTestApplication {
    fun setStateAndAttributes(json: String)
    fun lastApiCommandFrom(entity: Actuator<*, *>): String
}
