package khome.extending.notifications

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.entities.EntityId
import khome.extending.Domain.NOTIFY

private const val REQUEST_LOCATION_UPDATE = "request_location_update"

fun KhomeApplication.notifyMobileApp(device: Enum<*>, message: String, title: String? = null) =
    notifyMobileApp(device.name, message, title)

fun KhomeApplication.notifyMobileApp(device: String, message: String, title: String? = null) =
    callService(
        domain = NOTIFY.name,
        service = device,
        parameterBag = NotificationMessage(message = message, title = title)
    )

fun KhomeApplication.notifyMobileApp(device: Enum<*>, messageBuilder: NotificationWithDataMessage.() -> Unit) =
    notifyMobileApp(device.name, messageBuilder)

inline fun KhomeApplication.notifyMobileApp(device: String, messageBuilder: NotificationWithDataMessage.() -> Unit) =
    callService(
        domain = NOTIFY.name,
        service = device,
        parameterBag = NotificationWithDataMessage().apply(messageBuilder)
    )

fun KhomeApplication.notifyMobileApp(vararg devices: Enum<*>, title: String, message: String) =
    devices.forEach { device -> notifyMobileApp(device, message, title) }

fun KhomeApplication.notifyMobileApp(vararg devices: String, title: String, message: String) =
    devices.forEach { device -> notifyMobileApp(device, message, title) }

fun KhomeApplication.notifyMobileApp(
    vararg devices: Enum<*>,
    messageBuilder: NotificationWithDataMessage.() -> Unit
) = devices.forEach { device -> notifyMobileApp(device, messageBuilder) }

fun KhomeApplication.requestLocationUpdate(device: String) =
    notifyMobileApp(device, message = REQUEST_LOCATION_UPDATE)

fun KhomeApplication.requestLocationUpdate(device: Enum<*>) =
    notifyMobileApp(device, message = REQUEST_LOCATION_UPDATE)

fun KhomeApplication.requestLocationUpdate(vararg devices: String) =
    devices.forEach { device -> notifyMobileApp(device, message = REQUEST_LOCATION_UPDATE) }

fun KhomeApplication.requestLocationUpdate(vararg devices: Enum<*>) =
    devices.forEach { device -> notifyMobileApp(device, message = REQUEST_LOCATION_UPDATE) }

data class NotificationMessage(
    val title: String? = null,
    val message: String
)

class NotificationWithDataMessage {
    var title: String? = null
    lateinit var message: String
    private val data: MessageData = MessageData()
    fun data(builder: MessageData.() -> Unit) = data.apply(builder)
}

class MessageData {
    lateinit var subtitle: String
    private val push: PushData = PushData()
    var apnsHeaders: ApnsHeaders? = null
    var presentationOptions: List<PresentationOptions>? = null
    private var attachment: AttachmentData? = null

    var actionData: Any? = null
    var entityId: EntityId? = null

    enum class PresentationOptions {
        ALERT, BATCH, SOUND
    }

    fun push(builder: PushData.() -> Unit) = push.apply(builder)
    fun mapActionData(builder: MapActionData.() -> Unit) {
        actionData = MapActionData().apply(builder)
    }

    fun attachment(url: String? = null, contentType: String? = null, hideThumbnail: Boolean = false) {
        attachment = AttachmentData(url = url, contentType = contentType, hideThumbnail = hideThumbnail)
    }
}

data class AttachmentData(
    val url: String?,
    @SerializedName("content-type")
    val contentType: String?,
    @SerializedName("hide-thumbnail")
    val hideThumbnail: Boolean?
)

class PushData {
    @SerializedName("thread-id")
    var threadId: String? = null
    private var sound: SoundData? = null
    var badge: Int? = null
    var category: String? = null

    fun sound(name: String = "default", critical: Int? = null, volume: Double? = null) {
        sound = SoundData(name, critical, volume)
    }
}

data class SoundData(var name: String, var critical: Int?, var volume: Double?)

data class MapActionData(
    var latitude: String? = null,
    var longitude: String? = null,
    var secondLatitude: String? = null,
    var secondLongitude: String? = null,
    var showLineBetweenPoints: Boolean? = null,
    var showsCompass: Boolean? = null,
    var showsPointOfInterest: Boolean? = null,
    var showsScale: Boolean? = null,
    var showsTraffic: Boolean? = null,
    var showsUsersLocation: Boolean? = null
)

data class ApnsHeaders(@SerializedName("apns-collapse-id") var id: String? = null)
