package khome.communicating

import khome.KhomeApplicationImpl

interface CommandDispatcher<SD> {
    var commandData: SD?
}

internal class CommandDispatcherImpl<SD>(private val app: KhomeApplicationImpl) : CommandDispatcher<SD> {
    override var commandData: SD? = null
        set(newCommandData) {
            newCommandData?.let { data -> app.enqueueServiceCommand(this, data) }
            field = newCommandData
        }
}

data class ServiceId(val domain: String, val service: String)
