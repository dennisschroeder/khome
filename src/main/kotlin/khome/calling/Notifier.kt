package khome.calling

import com.google.gson.annotations.SerializedName

abstract class Notify : ServiceCall(Domain.NOTIFY, NotifyService.NOTIFY) {
    val serviceData: NotifierMessage = NotifierMessage()
    fun serviceData(builder: NotifierMessage.() -> Unit) = serviceData.apply(builder)
}

data class NotifierMessage(
    var message: String? = null,
    var title: String? = null,
    @SerializedName("target") var targets: List<String>? = null,
    var data: Map<String, Any>? = null
) : ServiceDataInterface

enum class NotifyService : ServiceInterface {
    NOTIFY
}
