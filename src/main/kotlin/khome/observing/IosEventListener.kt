package khome.observing

import khome.KhomeComponent
import khome.core.events.ios.IosActionEvent
import khome.core.events.ios.IosNotificationEvent

@ExperimentalStdlibApi
inline fun KhomeComponent.onIosActionEvent(crossinline callback: suspend (IosActionEventDataInterface) -> Unit) {
    onHassEvent<IosActionEvent> { eventData ->
        val iosActionEventData = IosActionEventData(
            sourceDeviceID = eventData["sourceDeviceID"] as String,
            actionID = eventData["actionID"]?.let { it as String },
            actionName = eventData["actionName"] as String,
            sourceDeviceName = eventData["sourceDeviceName"] as String,
            sourceDevicePermanentID = eventData["sourceDevicePermanentID"] as String,
            triggerSource = eventData["triggerSource"]?.let { it as String },
            actionData = eventData["action_data"]?.let { it as Map<String, Any> }
        )
        callback(iosActionEventData)
    }
}

@ExperimentalStdlibApi
inline fun KhomeComponent.onIosNotificationActionEvent(crossinline callback: suspend (IosActionEventDataInterface) -> Unit) {
    onHassEvent<IosNotificationEvent> { eventData ->
        val iosNotificationEventData = IosActionEventData(
            sourceDeviceID = eventData["sourceDeviceID"] as String,
            actionName = eventData["actionName"] as String,
            sourceDeviceName = eventData["sourceDeviceName"] as String,
            sourceDevicePermanentID = eventData["sourceDevicePermanentID"] as String,
            actionData = eventData["action_data"]?.let { it as Map<String, Any> }
        )
        callback(iosNotificationEventData)
    }
}

data class IosActionEventData(
    override val sourceDeviceID: String,
    override val actionID: String? = null,
    override val actionName: String,
    override val sourceDeviceName: String,
    override val sourceDevicePermanentID: String,
    override val triggerSource: String? = null,
    override val actionData: Map<String, Any>? = null
) : IosActionEventDataInterface

interface IosActionEventDataInterface {
    val sourceDeviceID: String
    val actionID: String?
    val actionName: String
    val sourceDeviceName: String
    val sourceDevicePermanentID: String
    val triggerSource: String?
    val actionData: Map<String, Any>?
}
