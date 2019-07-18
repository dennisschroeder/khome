package khome.calling

import khome.core.entities.EntityInterface

/**
 * Turns on an entity that is capable of being turned on.
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 *
 * @param entity An object that inherits the [EntityInterface] that represents an entity in home-assistant.
 */
fun ServiceCaller.turnOn(entity: EntityInterface) {
    domain = Domain.HOMEASSISTANT
    service = HomeAssistantServices.TURN_ON
    serviceData = EntityId(entity.id)
}

/**
 * Turns off an entity that is capable of being turned off.
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 *
 * @param entity An object that inherits the [EntityInterface] that represents an entity in home-assistant.
 */
fun ServiceCaller.turnOff(entity: EntityInterface) {
    domain = Domain.HOMEASSISTANT
    service = HomeAssistantServices.TURN_OFF
    serviceData = EntityId(entity.id)
}

/**
 * Toggles an entity that is capable of being turned on and off.
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 *
 * @param entity An object that inherits the [EntityInterface] that represents an entity in home-assistant.
 */
fun ServiceCaller.toggle(entity: EntityInterface) {
    domain = Domain.HOMEASSISTANT
    service = HomeAssistantServices.TOGGLE
    serviceData = EntityId(entity.id)
}

/**
 * Updates an entity..
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 *
 * @param entity An object that inherits the [EntityInterface] that represents an entity in home-assistant.
 */
fun ServiceCaller.updateEntity(entity: EntityInterface) {
    domain = Domain.HOMEASSISTANT
    service = HomeAssistantServices.UPDATE_ENTITY
    serviceData = EntityId(entity.id)
}

/**
 * Updates a bunch of entities.
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 *
 * @param entities Objects that inherits the [EntityInterface] that represents an entity in home-assistant.
 */
fun ServiceCaller.updateEntities(vararg entities: EntityInterface) {
    domain = Domain.HOMEASSISTANT
    service = HomeAssistantServices.UPDATE_ENTITY
    serviceData = EntityIds(listOf(*entities).joinToString(","), null)
}

/**
 * Stops the home-assistant instance.
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 */
fun ServiceCaller.stopHomeAssistant() {
    domain = Domain.HOMEASSISTANT
    service = HomeAssistantServices.STOP
}

/**
 * Restarts the home-assistant instance.
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 */
fun ServiceCaller.restartHomeAssistant() {
    domain = Domain.HOMEASSISTANT
    service = HomeAssistantServices.RESTART
}

internal enum class HomeAssistantServices : ServiceInterface {
    TURN_ON, TURN_OFF, TOGGLE, UPDATE_ENTITY, STOP, RESTART
}