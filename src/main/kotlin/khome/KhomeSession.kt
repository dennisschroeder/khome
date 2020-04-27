package khome

import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import io.ktor.util.KtorExperimentalAPI
import khome.calling.ServiceDataInterface
import khome.core.MessageInterface
import khome.core.boot.BootSequenceInterface
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.mapping.ObjectMapper
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging
import org.koin.core.get
import org.koin.core.parameter.parametersOf

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
class KhomeSession(
    delegate: DefaultClientWebSocketSession
) : KhomeKoinComponent(), WebSocketSession by delegate {
    val logger = KotlinLogging.logger {}
    suspend fun callWebSocketApi(message: String) = send(message)
    suspend fun callWebSocketApi(message: MessageInterface) = send(message.toJson())

    suspend inline fun <reified M : Any> consumeSingleMessage(): M = incoming.receive().asObject()
    inline fun <reified M : Any> Frame.asObject() = (this as Frame.Text).toObject<M>()
    inline fun <reified M : Any> Frame.Text.toObject(): M = get<ObjectMapper>().fromJson(readText())

    fun ServiceDataInterface.toJson(): String = get<ObjectMapper>().toJson(this)
    fun MessageInterface.toJson(): String = get<ObjectMapper>().toJson(this)

    suspend inline fun <reified BootSequence : BootSequenceInterface> runBootSequence() =
        get<BootSequence> { parametersOf(this) }.runBootSequence()
}
