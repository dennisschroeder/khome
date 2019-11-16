package khome

import com.google.gson.Gson
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import io.ktor.util.KtorExperimentalAPI
import khome.calling.ServiceDataInterface
import khome.core.MessageInterface
import khome.core.dependencyInjection.KhomeComponent
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.get
import kotlin.reflect.KClass

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
class KhomeSession(delegate: DefaultClientWebSocketSession) : KhomeComponent(), WebSocketSession by delegate {
    suspend fun callWebSocketApi(message: String) = send(message)
    suspend inline fun <reified M : Any> consumeMessage(): M = incoming.receive().asObject()
    inline fun <reified M : Any> Frame.asObject() = (this as Frame.Text).toObject<M>()
    inline fun <reified M : Any> Frame.Text.toObject(): M = toObject(M::class)

    fun <M : Any> Frame.Text.toObject(type: KClass<M>): M = get<Gson>().fromJson<M>(readText(), type.java)

    fun ServiceDataInterface.toJson(): String = get<Gson>().toJson(this)
    fun MessageInterface.toJson(): String = get<Gson>().toJson(this)
}
