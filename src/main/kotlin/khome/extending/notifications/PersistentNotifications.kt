package khome.extending.notifications

import khome.KhomeApplication
import khome.extending.callService
import khome.extending.Domain.PERSISTENT_NOTIFICATION
import khome.extending.notifications.PersistentNotification.CREATE
import khome.extending.notifications.PersistentNotification.DISMISS
import khome.extending.notifications.PersistentNotification.MARK_READ

fun KhomeApplication.createPersistentNotification(message: String, title: String? = null, notificationId: String? = null) =
    callService(PERSISTENT_NOTIFICATION, CREATE, PersistentNotificationMessage(message, title, notificationId))

fun KhomeApplication.dismissPersistentNotification(notificationId: String) =
    callService(PERSISTENT_NOTIFICATION, DISMISS, PersistentNotificationId(notificationId))

fun KhomeApplication.markPersistentNotificationAsRead(notificationId: String) =
    callService(PERSISTENT_NOTIFICATION, MARK_READ, PersistentNotificationId(notificationId))

internal data class PersistentNotificationMessage(
    val message: String,
    val title: String?,
    val notificationId: String?
)

internal data class PersistentNotificationId(val notificationId: String)

internal enum class PersistentNotification {
    CREATE, DISMISS, MARK_READ
}
