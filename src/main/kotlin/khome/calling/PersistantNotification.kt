package khome.calling

abstract class PersistentNotification :
    ServiceCall(Domain.PERSISTENT_NOTIFICATION, PersistentNotificationService.CREATE) {
    override val serviceData: PersistentNotificationMessage = PersistentNotificationMessage()
    fun serviceData(builder: PersistentNotificationMessage.() -> Unit) = serviceData.apply(builder)
}

data class PersistentNotificationMessage(
    var notificationId: Int? = null,
    var message: String? = null,
    var title: String? = null
) : ServiceDataInterface

enum class PersistentNotificationService : ServiceInterface {
    CREATE, DISMISS, MARK_READ
}
