package khome.extending.serviceCalls.notifications

import khome.KhomeApplication
import khome.extending.callService
import khome.extending.serviceCalls.Domain.PERSISTENT_NOTIFICATION
import khome.extending.serviceCalls.notifications.PersistentNotification.CREATE
import khome.extending.serviceCalls.notifications.PersistentNotification.DISMISS
import khome.extending.serviceCalls.notifications.PersistentNotification.MARK_READ

fun KhomeApplication.createPersistentNotification(message: String, title: String? = null, notificationId: String? = null) =
    callService(PERSISTENT_NOTIFICATION, CREATE,
        PersistentNotificationMessage(
            message,
            title,
            notificationId
        )
    )

fun KhomeApplication.dismissPersistentNotification(id: String) =
    callService(PERSISTENT_NOTIFICATION, DISMISS,
        PersistentNotificationId(id)
    )

fun KhomeApplication.markPersistentNotificationAsRead(id: String) =
    callService(PERSISTENT_NOTIFICATION, MARK_READ,
        PersistentNotificationId(id)
    )

internal data class PersistentNotificationMessage(
    val message: String,
    val title: String?,
    val notificationId: String?
)

internal data class PersistentNotificationId(val notificationId: String)

internal enum class PersistentNotification {
    CREATE, DISMISS, MARK_READ
}
