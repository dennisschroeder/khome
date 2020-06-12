package khome.observability

inline fun onIosActionEvent(crossinline callback: suspend (IosActionEventDataInterface) -> Unit) {
}

inline fun onIosNotificationActionEvent(crossinline callback: suspend (IosActionEventDataInterface) -> Unit) {
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
