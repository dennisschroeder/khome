package khome.calling

/**
 * Turns on an entity that is capable of being turned on.
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 *
 * @param entity An object that inherits the [EntitySubjectInterface] that represents an entity in home-assistant.
 */
class TurnOn :
    EntityIdOnlyServiceCall(HassDomain.HOMEASSISTANT, HomeAssistantService.TURN_ON)

/**
 * Turns off an entity that is capable of being turned off.
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 *
 * @param entity An object that inherits the [EntitySubjectInterface] that represents an entity in home-assistant.
 */
class TurnOff :
    EntityIdOnlyServiceCall(HassDomain.HOMEASSISTANT, HomeAssistantService.TURN_OFF)

/**
 * Toggles an entity that is capable of being turned on and off.
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 *
 * @param entity An object that inherits the [EntitySubjectInterface] that represents an entity in home-assistant.
 */

class Toggle :
    EntityIdOnlyServiceCall(HassDomain.HOMEASSISTANT, HomeAssistantService.TOGGLE)

/**
 * Updates an entity..
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 *
 * @param entity An object that inherits the [EntitySubjectInterface] that represents an entity in home-assistant.
 */

class UpdateEntity :
    EntityIdOnlyServiceCall(HassDomain.HOMEASSISTANT, HomeAssistantService.UPDATE_ENTITY)

/**
 * Stops the home-assistant instance.
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 */

class StopHomeAssistant : ServiceCall(
    HassDomain.HOMEASSISTANT,
    HomeAssistantService.STOP
) {
    val serviceData: ServiceDataInterface? = null
}

/**
 * Restarts the home-assistant instance.
 * More on [that](https://www.home-assistant.io/docs/scripts/service-calls/) in the official home-assistant documentation.
 */
class RestartHomeAssistant : ServiceCall(
    HassDomain.HOMEASSISTANT,
    HomeAssistantService.RESTART
) {
    val serviceData: ServiceDataInterface? = null
}

enum class HomeAssistantService : ServiceInterface {
    TURN_ON, TURN_OFF, TOGGLE, UPDATE_ENTITY, STOP, RESTART
}
