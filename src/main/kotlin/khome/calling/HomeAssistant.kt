package khome.calling

fun ServiceCaller.turnOn(entityId: String) {
    domain = "homeassistant"
    service = "turn_on"
    serviceData = EntityId(entityId)
}

fun ServiceCaller.turnOff(entityId: String) {
    domain = "homeassistant"
    service = "turn_off"
    serviceData = EntityId(entityId)
}

fun ServiceCaller.toggle(entityId: String) {
    domain = "homeassistant"
    service = "toggle"
    serviceData = EntityId(entityId)
}

fun ServiceCaller.updateEntity(entityId: String) {
    domain = "homeassistant"
    service = "update_entity"
    serviceData = EntityId(entityId)
}

fun ServiceCaller.updateEntities(vararg entityIds: String) {
    domain = "homeassistant"
    service = "update_entity"
    serviceData = EntityIds(listOf(*entityIds))
}

fun ServiceCaller.stopHomeAssistant() {
    domain = "homeassistant"
    service = "restart"
}

fun ServiceCaller.restartHomeAssistant() {
    domain = "homeassistant"
    service = "restart"
}