package khome

import io.ktor.http.HttpMethod
import io.ktor.util.KtorExperimentalAPI
import khome.core.ConfigurationInterface
import khome.core.clients.WebSocketClient
import khome.core.dependencyInjection.KhomeKoinComponent
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.get
import org.koin.core.parameter.parametersOf

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
class KhomeClient(
    private val config: ConfigurationInterface,
    private val httpClient: WebSocketClient
) : KhomeKoinComponent() {

    private val method = HttpMethod.Get
    private val path = "/api/websocket"
    private val isSecure: Boolean = config.secure

    @ObsoleteCoroutinesApi
    suspend fun startSession(block: suspend KhomeSession.() -> Unit) =
        when (isSecure) {
            true -> httpClient.secureWebsocket(
                method = method,
                host = config.host,
                port = config.port,
                path = path,
                block = { block(get { parametersOf(this) }) })
            false -> httpClient.websocket(
                method = method,
                host = config.host,
                port = config.port,
                path = path,
                block = { block(get { parametersOf(this) }) })
        }
}
