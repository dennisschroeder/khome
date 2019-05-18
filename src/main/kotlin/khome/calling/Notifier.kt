package khome.calling

import khome.listening.getEntityInstance
import khome.core.entities.EntityInterface
import com.google.gson.annotations.SerializedName

inline fun <reified Entity : EntityInterface> ServiceCaller.notifyIOS(noinline init: NotifierMessage.() -> Unit) {
    val entity = getEntityInstance<Entity>()
    notifyIOS(entity.name, init)
}

fun ServiceCaller.notifyIOS(deviceId: String, init: NotifierMessage.() -> Unit) {
    domain = "notify"
    service = "ios_$deviceId"
    serviceData = NotifierMessage(
        null,
        null,
        null,
        null
    ).apply(init)
}

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