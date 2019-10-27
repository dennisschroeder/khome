package khome.calling

import com.google.gson.annotations.SerializedName

fun ServiceCaller.notify(init: NotifierMessage.() -> Unit) {
    domain = Domain.NOTIFY
    serviceData = NotifierMessage(
        null,
        null,
        null,
        null,
        null
    ).apply(init)
}

data class NotifierMessage(
    override var entityId: String?,
    var message: String?,
    var title: String?,
    @SerializedName("target") var targets: List<String>?,
    var data: NotifierDataInterface?
) : ServiceDataInterface

interface NotifierDataInterface

enum class NotifierServices : ServiceInterface {
    NOTIFY
}
