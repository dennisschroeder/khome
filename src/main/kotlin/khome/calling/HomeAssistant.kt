package khome.calling

import khome.core.entities.EntityInterface
import khome.listening.getEntityInstance

fun ServiceCaller.turnOn(entityId: String) {
    domain = Domain.HOMEASSISTANT
    service = HomeAssistantServices.TURN_ON
    serviceData = EntityId(entityId)
}

inline fun <reified Entity : EntityInterface> ServiceCaller.turnOn() {
    val entity = getEntityInstance<Entity>()

    turnOn(entity.id)
}

fun ServiceCaller.turnOff(entityId: String) {
    domain = Domain.HOMEASSISTANT
    service = HomeAssistantServices.TURN_OFF
    serviceData = EntityId(entityId)
}

inline fun <reified Entity : EntityInterface> ServiceCaller.turnOff() {
    val entity = getEntityInstance<Entity>()

    turnOff(entity.id)
}

fun ServiceCaller.toggle(entityId: String) {
    domain = Domain.HOMEASSISTANT
    service = HomeAssistantServices.TOGGLE
    serviceData = EntityId(entityId)
}

inline fun <reified Entity : EntityInterface> ServiceCaller.toggle() {
    val entity = getEntityInstance<Entity>()

    toggle(entity.id)
}

fun ServiceCaller.updateEntity(entityId: String) {
    domain = Domain.HOMEASSISTANT
    service = HomeAssistantServices.UPDATE_ENTITY
    serviceData = EntityId(entityId)
}

inline fun <reified Entity : EntityInterface> ServiceCaller.updateEntity() {
    val entity = getEntityInstance<Entity>()

    updateEntity(entity.id)
}

fun ServiceCaller.updateEntities(vararg entityIds: String) {
    domain = Domain.HOMEASSISTANT
    service = HomeAssistantServices.UPDATE_ENTITY
    serviceData = EntityIds(listOf(*entityIds).joinToString(","), null)
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