package khome.extending

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.events.AsyncEventHandlerFunction
import khome.events.EventHandlerFunction
import khome.extending.IosEventType.ACTION_FIRED
import khome.extending.IosEventType.NOTIFICATION_ACTION_FIRED
import khome.observability.Switchable

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

fun KhomeApplication.attachIosActionHandler(eventHandler: EventHandlerFunction<IosActionEventData>): Switchable =
    attachEventHandler(ACTION_FIRED.type, eventHandler)

fun KhomeApplication.attachAsyncIosActionHandler(eventHandler: AsyncEventHandlerFunction<IosActionEventData>): Switchable =
    attachAsyncEventHandler(ACTION_FIRED.type, eventHandler)

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

fun <AD> KhomeApplication.attachIosNotificationActionHandler(eventHandler: EventHandlerFunction<IosNotificationActionEventData<AD>>): Switchable =
    attachEventHandler(NOTIFICATION_ACTION_FIRED.type, eventHandler)

fun <AD> KhomeApplication.attachAsyncIosNotificationActionHandler(eventHandler: AsyncEventHandlerFunction<IosNotificationActionEventData<AD>>): Switchable =
    attachAsyncEventHandler(NOTIFICATION_ACTION_FIRED.type, eventHandler)

internal enum class IosEventType(val type: String) {
    ACTION_FIRED("ios.action_fired"),
    NOTIFICATION_ACTION_FIRED("ios.notification_action_fired")
}