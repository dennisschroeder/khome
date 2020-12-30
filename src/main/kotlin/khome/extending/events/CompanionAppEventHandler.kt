package khome.extending.events

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.events.EventHandlerFunction
import khome.extending.events.IosEventType.ACTION_FIRED
import khome.extending.events.IosEventType.NOTIFICATION_ACTION_FIRED
import khome.values.EventType

fun KhomeApplication.attachIosActionHandler(eventHandler: EventHandlerFunction<IosActionEventData>) =
    attachEventHandler(ACTION_FIRED.eventType, eventHandler)

data class IosActionEventData(
    @SerializedName("sourceDeviceID")
    val sourceDeviceID: String,

    @SerializedName("actionID")
    val actionID: String,

    @SerializedName("actionName")
    val actionName: String,

    @SerializedName("sourceDeviceName")
    val sourceDeviceName: String,

    @SerializedName("sourceDevicePermanentID")
    val sourceDevicePermanentID: String,

    @SerializedName("triggerSource")
    val triggerSource: String
)

data class IosNotificationActionEventData<AD>(
    @SerializedName("sourceDeviceName")
    val sourceDeviceName: String,

    @SerializedName("sourceDeviceID")
    val sourceDeviceID: String,

    @SerializedName("actionName")
    val actionName: String,

    @SerializedName("sourceDevicePermanentID")
    val sourceDevicePermanentID: String?,

    @SerializedName("textInput")
    val textInput: String?,

    @SerializedName("action_data")
    val actionData: AD?
)

fun <AD> KhomeApplication.attachIosNotificationActionHandler(eventHandler: EventHandlerFunction<IosNotificationActionEventData<AD>>) =
    attachEventHandler(NOTIFICATION_ACTION_FIRED.eventType, eventHandler)

internal enum class IosEventType(val value: String) {
    ACTION_FIRED("ios.action_fired"),
    NOTIFICATION_ACTION_FIRED("ios.notification_action_fired")
}

internal val IosEventType.eventType
    get() = EventType.from(this.value)
