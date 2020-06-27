package khome.extending.notifications

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.extending.Domain.NOTIFY

fun KhomeApplication.notifyDevice(device: Enum<*>, message: String, title: String? = null) =
    notifyDevice(device.name, message, title)

fun KhomeApplication.notifyDevice(device: String, message: String, title: String? = null) =
    callService(
        domain = NOTIFY.name,
        service = device,
        parameterBag = NotificationMessage(message = message, title = title)
    )

fun KhomeApplication.notifyDevice(device: Enum<*>, messageBuilder: NotificationWithDataMessage.() -> Unit) =
    notifyDevice(device.name, messageBuilder)

inline fun KhomeApplication.notifyDevice(device: String, messageBuilder: NotificationWithDataMessage.() -> Unit) =
    callService(
        domain = NOTIFY.name,
        service = device,
        parameterBag = NotificationWithDataMessage().apply(messageBuilder)
    )

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

    enum class PresentationOptions {
        ALERT, BATCH, SOUND
    }

    fun push(builder: PushData.() -> Unit) = push.apply(builder)
    fun mapActionData(builder: MapActionData.() -> Unit) {
        actionData = MapActionData().apply(builder)
    }
    fun attachment(url: String, contentType: String? = null, hideThumbnail: Boolean = false) {
        attachment = AttachmentData(url = url, contentType = contentType, hideThumbnail = hideThumbnail)
    }
}

data class AttachmentData(
    val url: String,
    @SerializedName("content-type")
    val contentType: String?,
    @SerializedName("hide-thumbnail")
    val hideThumbnail: Boolean?
)

class PushData {
    @SerializedName("thread-id")
    var threadId: String? = null
    var sound: SoundData? = null
    var badge: Int? = null
    var category: String? = null

    fun sound(critical: Int, volume: Double, name: String = "default") {
        sound = SoundData(name, critical, volume)
    }
}

data class SoundData(var name: String, var critical: Int, var volume: Double)

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
