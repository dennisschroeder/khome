package khome.values

import khome.core.mapping.KhomeTypeAdapter
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
@Suppress("DataClassPrivateConstructor")
data class EntityId private constructor(val domain: Domain, val objectId: ObjectId) {

    /**
     * The EntityId's string representation.
     *
     * Equals the value that is used to communicate with
     * home assistant.
     */
    override fun toString(): String = "${domain.value}.${objectId.value}"

    companion object : KhomeTypeAdapter<EntityId> {

        fun fromPair(pair: Pair<Domain, ObjectId>) =
            from("${pair.first}.${pair.second}")

        fun fromString(value: String): EntityId {
            val parts = value.split(".")
            check(parts.size == 2) { "EntityId has wrong format. Correct format is: \"domain.objectId\"" }
            val (domain, id) = parts
            return EntityId(Domain.from(domain), ObjectId.from(id))
        }

        override fun <P> from(value: P): EntityId =
            fromString(value as String)

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: EntityId): P {
            return value.toString() as P
        }
    }
}
