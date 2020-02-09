package khome

import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.ws
import io.ktor.client.features.websocket.wss
import io.ktor.http.HttpMethod
import io.ktor.util.KtorExperimentalAPI
import khome.core.ConfigurationInterface
import khome.core.dependencyInjection.KhomeKoinComponent
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.get
import org.koin.core.parameter.parametersOf

@KtorExperimentalAPI
class KhomeClient(
    private val config: ConfigurationInterface,
    private val httpClient: HttpClient
) : KhomeKoinComponent() {

    private val method = HttpMethod.Get
    private val path = "/api/websocket"
    private val isSecure: Boolean = config.secure

    @ObsoleteCoroutinesApi
    suspend fun startSession(block: suspend KhomeSession.() -> Unit) =
        when (isSecure) {
            true -> httpClient.wss(
                method = method,
                host = config.host,
                port = config.port,
                path = path,
                block = { block(get { parametersOf(this) }) })
            false -> httpClient.ws(
                method = method,
                host = config.host,
                port = config.port,
                path = path,
                block = { block(get { parametersOf(this) }) })
        }
}
