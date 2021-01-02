package khome.extending.serviceCalls.notifications

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.values.Device
import khome.values.EntityId
import khome.values.Service
import khome.values.domain

private const val REQUEST_LOCATION_UPDATE = "request_location_update"

fun KhomeApplication.notifyMobileApp(device: Device, message: String, title: String? = null) =
    callService(
        domain = "notify".domain,
        service = Service.fromDevice(device),
        parameterBag = NotificationMessage(
            message = message,
            title = title
        )
    )

inline fun KhomeApplication.notifyMobileApp(device: Device, messageBuilder: NotificationWithDataMessage.() -> Unit) =
    callService(
        domain = "notify".domain,
        service = Service.fromDevice(device),
        parameterBag = NotificationWithDataMessage().apply(messageBuilder)
    )

fun KhomeApplication.notifyMobileApp(vararg devices: Device, title: String, message: String) =
    devices.forEach { device -> notifyMobileApp(device, message, title) }

fun KhomeApplication.requestLocationUpdate(device: Device) =
    notifyMobileApp(device, message = REQUEST_LOCATION_UPDATE)

fun KhomeApplication.requestLocationUpdate(vararg devices: Device) =
    devices.forEach { device -> notifyMobileApp(device, message = REQUEST_LOCATION_UPDATE) }

data class NotificationMessage(
    val title: String? = null,
    val message: String
)

class NotificationWithDataMessage {
    var title: String? = null
    lateinit var message: String
    private val data: MessageData =
        MessageData()
    fun data(builder: MessageData.() -> Unit) = data.apply(builder)
}

class MessageData {
    lateinit var subtitle: String
    private val push: PushData =
        PushData()
    var apnsHeaders: ApnsHeaders? = null
    var presentationOptions: List<PresentationOptions>? = null
    private var attachment: AttachmentData? = null

    private var actionData: Any? = null
    var entityId: EntityId? = null

    enum class PresentationOptions {
        ALERT, BATCH, SOUND
    }

    fun push(builder: PushData.() -> Unit) = push.apply(builder)
    fun mapActionData(builder: MapActionData.() -> Unit) {
        actionData = MapActionData().apply(builder)
    }

    fun attachment(url: String? = null, contentType: String? = null, hideThumbnail: Boolean = false) {
        attachment = AttachmentData(
            url = url,
            contentType = contentType,
            hideThumbnail = hideThumbnail
        )
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
