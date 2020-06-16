package khome.entities

import khome.entities.devices.Actuator
import khome.entities.devices.Sensor

/**
 * The EntityId of an [Actuator] or [Sensor]
 *
 * The entity id is not a member of an [Actuator] or [Sensor] but is used
 * as a key in the registries.
 *
 * @property domain the domain that the entity belongs to e.g. cover, light, sensor
 * @property objectId the object id of an entity
 */
data class EntityId(val domain: String, val objectId: String) {

    /**
     * The EntityId's string representation.
     *
     * Equals the value that is used to communicate with
     * home assistant.
     */
    override fun toString(): String = "$domain.$objectId"

    internal companion object {
        fun fromString(apiName: String): EntityId {
            val parts = apiName.split(".")
            assert(parts.size == 2)
            val (domain, id) = parts
            return EntityId(domain, id)
        }
    }
}
