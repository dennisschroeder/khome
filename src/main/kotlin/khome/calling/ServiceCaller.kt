package khome.calling

import khome.*
import khome.core.logger
import kotlinx.coroutines.*
import khome.core.serializer
import khome.core.MessageInterface
import khome.Khome.Companion.services
import khome.Khome.Companion.idCounter
import khome.core.entities.EntityInterface
import khome.Khome.Companion.callServiceContext
import khome.Khome.Companion.isSandBoxModeActive
import com.google.gson.annotations.SerializedName
import io.ktor.http.cio.websocket.WebSocketSession
import khome.calling.exceptions.DomainNotFoundException
import khome.calling.exceptions.ServiceNotFoundException

@ObsoleteCoroutinesApi
fun WebSocketSession.callService(init: ServiceCaller.() -> Unit) {
    runBlocking {
        withContext(callServiceContext) {
            val callService = ServiceCaller(
                idCounter.incrementAndGet(),
                "call_service",
                null,
                null,
                null
            ).apply(init)

            when {
                isSandBoxModeActive -> {
                    val domain = callService.domain.toString().toLowerCase()
                    val service = callService.service.toString().toLowerCase()

                    when {
                        domain !in services -> throw DomainNotFoundException("$domain is not an registered domain in homeassistant")
                        service !in services[domain]!! -> throw ServiceNotFoundException("$service is not an available service under $domain in homeassistant")
                    }
                }
                else -> {
                    callWebSocketApi(callService.toJson())
                    logger.info { "Called Service with: " + callService.toJson() }
                }
            }
        }
    }
}

data class EntityId(override var entityId: String?) : ServiceDataInterface

data class EntityIds(
    @SerializedName("entity_id") var entityIds: String,
    override var entityId: String?
) : ServiceDataInterface

data class ServiceCaller(
    private var id: Int,
    override val type: String = "call_service",
    var domain: DomainInterface?,
    var service: ServiceInterface?,
    var serviceData: ServiceDataInterface?
) : MessageInterface {
    fun entityId(entity: EntityInterface) {
        serviceData = EntityId(entity.id)
    }
}

interface ServiceDataInterface {
    var entityId: String?
    fun toJson(): String = serializer.toJson(this)
}

interface ServiceInterface
interface DomainInterface

enum class Domain : DomainInterface {
    COVER, LIGHT, HOMEASSISTANT, MEDIA_PLAYER, NOTIFY
}

