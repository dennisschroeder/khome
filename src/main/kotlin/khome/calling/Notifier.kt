package khome.calling

import com.google.gson.annotations.SerializedName

fun ServiceCaller.notify(init: NotifierMessage.() -> Unit) {
    domain = "notify"
    serviceData = NotifierMessage(
        null,
        null,
        null,
        null
    ).apply(init)
}

data class NotifierMessage(
    var message: String?,
    var title: String?,
    @SerializedName("target") var targets: List<String>?,
    var data: NotifierDataInterface?
) : ServiceDataInterface

interface NotifierDataInterface