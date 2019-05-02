package khome.calling

import khome.*
import khome.core.serializer
import kotlinx.coroutines.launch
import khome.core.MessageInterface
import kotlinx.coroutines.runBlocking
import khome.Khome.Companion.fetchNextId
import khome.Khome.Companion.incrementIdCounter
import io.ktor.http.cio.websocket.WebSocketSession
import khome.core.logger

fun WebSocketSession.callService(init: CallService.() -> Unit) {
    runBlocking {
        incrementIdCounter()
        val callService = CallService(
            fetchNextId(),
            "call_service",
            null,
            null,
            null
        ).apply(init)

        launch {
            callWebSocketApi(callService.toJson())
            logger.info { "Called  Service with: " + callService.toJson() }
        }
    }
}

fun CallService.entityId(entityId: String) {
    serviceData = EntityId(entityId)
}

data class EntityId(var entityId: String) : ServiceDataInterface

data class CallService(
    private var id: Int,
    override val type: String = "call_service",
    var domain: String?,
    var service: String?,
    var serviceData: ServiceDataInterface?
) : MessageInterface

interface ServiceDataInterface {
    fun toJson(): String = serializer.toJson(this)
}