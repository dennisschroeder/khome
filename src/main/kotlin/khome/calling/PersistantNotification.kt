package khome.calling

class PersistentNotificationCreate :
    ServiceCall(Domain.PERSISTENT_NOTIFICATION, PersistentNotificationService.CREATE) {
    val serviceData: PersistentNotificationMessage = PersistentNotificationMessage(null, null, null)
    fun configure(builder: PersistentNotificationMessage.() -> Unit) = serviceData.apply(builder)
}

class PersistentNotificationDismiss :
    ServiceCall(Domain.PERSISTENT_NOTIFICATION, PersistentNotificationService.DISMISS) {
    val serviceData: PersistentNotificationMessage = PersistentNotificationMessage(null, null, null)
    fun notificationId(id: String) = serviceData.apply { notificationId = id }
}

class PersistentNotificationMarkRead :
    ServiceCall(Domain.PERSISTENT_NOTIFICATION, PersistentNotificationService.MARK_READ) {
    val serviceData: PersistentNotificationMessage = PersistentNotificationMessage(null, null, null)
    fun notificationId(id: String) = serviceData.apply { notificationId = id }
}

data class PersistentNotificationMessage(
    var notificationId: String?,
    var message: String?,
    var title: String?
) : ServiceDataInterface

enum class PersistentNotificationService : ServiceInterface {
    CREATE, DISMISS, MARK_READ
}
