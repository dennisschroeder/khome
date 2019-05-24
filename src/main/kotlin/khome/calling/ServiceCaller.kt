package khome.calling

import khome.*
import khome.core.logger
import khome.core.serializer
import kotlinx.coroutines.launch
import khome.core.MessageInterface
import khome.Khome.Companion.idCounter
import com.google.gson.annotations.SerializedName
import io.ktor.http.cio.websocket.WebSocketSession

@Synchronized
fun WebSocketSession.callService(init: ServiceCaller.() -> Unit) {

    val callService = ServiceCaller(
        idCounter.incrementAndGet(),
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

fun ServiceCaller.entityId(entityId: String) {
    serviceData = EntityId(entityId)
}

data class EntityId(override var entityId: String?) : ServiceDataInterface

data class EntityIds(@SerializedName("entity_id") var entityIds: String, override var entityId: String?) :
    ServiceDataInterface

data class ServiceCaller(
    private var id: Int,
    override val type: String = "call_service",
    var domain: DomainInterface?,
    var service: ServiceInterface?,
    var serviceData: ServiceDataInterface?
) : MessageInterface

interface ServiceDataInterface {
    var entityId: String?
    fun toJson(): String = serializer.toJson(this)
}

interface ServiceInterface
interface DomainInterface

enum class Domain : DomainInterface {
    COVER, LIGHT, HOMEASSISTANT, MEDIA_PLAYER, NOTIFY
}