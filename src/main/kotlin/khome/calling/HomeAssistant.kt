package khome.calling

import khome.core.entities.EntityInterface


fun ServiceCaller.turnOn(entity: EntityInterface) {
    domain = Domain.HOMEASSISTANT
    service = HomeAssistantServices.TURN_ON
    serviceData = EntityId(entity.id)
}

fun ServiceCaller.turnOff(entity: EntityInterface) {
    domain = Domain.HOMEASSISTANT
    service = HomeAssistantServices.TURN_OFF
    serviceData = EntityId(entity.id)
}

fun ServiceCaller.toggle(entity: EntityInterface) {
    domain = Domain.HOMEASSISTANT
    service = HomeAssistantServices.TOGGLE
    serviceData = EntityId(entity.id)
}

fun ServiceCaller.updateEntity(entity: EntityInterface) {
    domain = Domain.HOMEASSISTANT
    service = HomeAssistantServices.UPDATE_ENTITY
    serviceData = EntityId(entity.id)
}

fun ServiceCaller.updateEntities(vararg entities: EntityInterface) {
    domain = Domain.HOMEASSISTANT
    service = HomeAssistantServices.UPDATE_ENTITY
    serviceData = EntityIds(listOf(*entities).joinToString(","), null)
}

fun ServiceCaller.stopHomeAssistant() {
    domain = Domain.HOMEASSISTANT
    service = HomeAssistantServices.STOP
}

fun ServiceCaller.restartHomeAssistant() {
    domain = Domain.HOMEASSISTANT
    service = HomeAssistantServices.RESTART
}

enum class HomeAssistantServices : ServiceInterface {
    TURN_ON, TURN_OFF, TOGGLE, UPDATE_ENTITY, STOP, RESTART
}