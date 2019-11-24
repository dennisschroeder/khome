package khome.calling

fun ServiceCaller.createPersistentNotification(init: PersistentNotificationMessage.() -> Unit) {
    domain = Domain.PERSISTENT_NOTIFICATION
    service = PersistentNotificationService.CREATE
    serviceData = PersistentNotificationMessage(
        null,
        null,
        null,
        null
    ).apply(init)
}

data class PersistentNotificationMessage(
    override var entityId: String?,
    var notificationId: Int?,
    var message: String?,
    var title: String?
) : ServiceDataInterface

enum class PersistentNotificationService : ServiceInterface {
    CREATE, DISMISS, MARK_READ
}