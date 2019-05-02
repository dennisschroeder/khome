package khome.calling

import khome.Khome.Companion.fetchNextId
import khome.Khome.Companion.incrementIdCounter
import khome.Khome.Companion.logger
import io.ktor.http.cio.websocket.WebSocketSession
import khome.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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

data class EntityId(var entityId: String) : ServiceData

data class CallService(
    private var id: Int?,
    override val type: String = "call_service",
    var domain: String?,
    var service: String?,
    var serviceData: ServiceData?
) : Message

interface ServiceData
fun ServiceData.toJson(): String = Khome.serializer.toJson(this)