package khome.calling

import khome.core.entities.EntityInterface
import khome.listening.getEntityInstance

fun ServiceCaller.turnOn(entityId: String) {
    domain = "homeassistant"
    service = "turn_on"
    serviceData = EntityId(entityId)
}

inline fun <reified Entity : EntityInterface> ServiceCaller.turnOn() {
    val entity = getEntityInstance<Entity>()

    turnOn(entity.entityId)
}

fun ServiceCaller.turnOff(entityId: String) {
    domain = "homeassistant"
    service = "turn_off"
    serviceData = EntityId(entityId)
}

inline fun <reified Entity : EntityInterface> ServiceCaller.turnOff() {
    val entity = getEntityInstance<Entity>()

    turnOff(entity.entityId)
}

fun ServiceCaller.toggle(entityId: String) {
    domain = "homeassistant"
    service = "toggle"
    serviceData = EntityId(entityId)
}

inline fun <reified Entity : EntityInterface> ServiceCaller.toggle() {
    val entity = getEntityInstance<Entity>()

    toggle(entity.entityId)
}

fun ServiceCaller.updateEntity(entityId: String) {
    domain = "homeassistant"
    service = "update_entity"
    serviceData = EntityId(entityId)
}

inline fun <reified Entity : EntityInterface> ServiceCaller.updateEntity() {
    val entity = getEntityInstance<Entity>()

    updateEntity(entity.entityId)
}

fun ServiceCaller.updateEntities(vararg entityIds: String) {
    domain = "homeassistant"
    service = "update_entity"
    serviceData = EntityIds(listOf(*entityIds).joinToString(","))
}

fun ServiceCaller.stopHomeAssistant() {
    domain = "homeassistant"
    service = "stop"
}

fun ServiceCaller.restartHomeAssistant() {
    domain = "homeassistant"
    service = "restart"
}