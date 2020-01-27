package khome.calling

import khome.core.entities.EntityInterface

/**
 * Turns on an entity that is capable of being turned on.
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 *
 * @param entity An object that inherits the [EntityInterface] that represents an entity in home-assistant.
 */
abstract class TurnOn(entity: EntityInterface) :
    ServiceCall(Domain.HOMEASSISTANT, HomeAssistantService.TURN_ON) {
    override val serviceData: ServiceDataInterface? = EntityId(entity.id)
}

/**
 * Turns off an entity that is capable of being turned off.
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 *
 * @param entity An object that inherits the [EntityInterface] that represents an entity in home-assistant.
 */
abstract class TurnOff(entity: EntityInterface) :
    ServiceCall(Domain.HOMEASSISTANT, HomeAssistantService.TURN_OFF) {
    override val serviceData: ServiceDataInterface? = EntityId(entity.id)
}

/**
 * Toggles an entity that is capable of being turned on and off.
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 *
 * @param entity An object that inherits the [EntityInterface] that represents an entity in home-assistant.
 */

abstract class Toggle(entity: EntityInterface) :
    ServiceCall(Domain.HOMEASSISTANT, HomeAssistantService.TOGGLE) {
    override val serviceData: ServiceDataInterface? = EntityId(entity.id)
}

/**
 * Updates an entity..
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 *
 * @param entity An object that inherits the [EntityInterface] that represents an entity in home-assistant.
 */

abstract class UpdateEntity(entity: EntityInterface) :
    ServiceCall(Domain.HOMEASSISTANT, HomeAssistantService.UPDATE_ENTITY) {
    override val serviceData: ServiceDataInterface? = EntityId(entity.id)
}

/**
 * Updates a bunch of entities.
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 *
 * @param entities Objects that inherits the [EntityInterface] that represents an entity in home-assistant.
 */

abstract class UpdateEntities(vararg entities: EntityInterface) : ServiceCall(
    Domain.HOMEASSISTANT,
    HomeAssistantService.UPDATE_ENTITY
) {
    override val serviceData: ServiceDataInterface? = EntityIds(listOf(*entities).joinToString(","))
}

/**
 * Stops the home-assistant instance.
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 */

abstract class StopHomeAssistant : ServiceCall(
    Domain.HOMEASSISTANT,
    HomeAssistantService.STOP
) {
    override val serviceData: ServiceDataInterface? = null
}

/**
 * Restarts the home-assistant instance.
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 */
abstract class RestartHomeAssistant : ServiceCall(
    Domain.HOMEASSISTANT,
    HomeAssistantService.RESTART
) {
    override val serviceData: ServiceDataInterface? = null
}

enum class HomeAssistantService : ServiceInterface {
    TURN_ON, TURN_OFF, TOGGLE, UPDATE_ENTITY, STOP, RESTART
}
