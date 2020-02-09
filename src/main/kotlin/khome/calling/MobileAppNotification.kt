package khome.calling

import com.google.gson.annotations.SerializedName
import io.ktor.http.URLBuilder

abstract class NotifyMobileApp(device: ServiceInterface) : ServiceCall(Domain.NOTIFY, device) {
    override val serviceData = NotificationMessage()
    fun serviceData(builder: NotificationMessage.() -> Unit) =
        serviceData.apply(builder)
}

data class NotificationMessage(
    var title: String? = null,
    var message: String? = null,
    private val data: MessageData = MessageData()

) : ServiceDataInterface {
    fun data(builder: MessageData.() -> Unit) = data.apply(builder)
}

data class MessageData(
    var subtitle: String? = null,
    private val push: PushData = PushData(),
    var apnsHeaders: ApnsHeaders? = null,
    var presentationOptions: List<PresentationOptions>? = null,
    private val attachment: AttachmentData = AttachmentData(),
    var actionData: ActionData = ActionData()
) {
    enum class PresentationOptions {
        ALERT, BATCH, SOUND
    }

    fun push(builder: PushData.() -> Unit) = push.apply(builder)
    fun actionData(builder: ActionData.() -> Unit) = actionData.apply(builder)
    fun attachment(builder: AttachmentData.() -> Unit) = attachment.apply(builder)
}

data class AttachmentData(
    private var url: String? = null,
    @SerializedName("content-type")
    var contentType: String? = null,
    @SerializedName("hide-thumbnail")
    var hideThumbnail: Boolean = false
) {
    fun setUrl(urlString: String) =
        URLBuilder(urlString).build().let { url = it.toString() }
}

data class PushData(
    @SerializedName("thread-id")
    var threadId: String? = null,
    var sound: String? = null,
    var badge: Int? = null,
    var category: PushCategoryInterface? = null
) {
    enum class PushCategory : PushCategoryInterface {
        MAP
    }
}

interface PushCategoryInterface

data class ActionData(
    var latitude: String? = null,
    var longitude: String? = null,
    var secondLatitude: String? = null,
    var secondLongitude: String? = null,
    var showLineBetweenPoints: Boolean? = null,
    var showsCompass: Boolean? = null,
    var showsPointOfInterest: Boolean? = null,
    var showsScale: Boolean? = null,
    var showsTraffic: Boolean? = null,
    var showsUsersLocation: Boolean? = null,
    var custom: Map<String, Any> = emptyMap()
)

data class ApnsHeaders(@SerializedName("apns-collapse-id") var id: String? = null)
