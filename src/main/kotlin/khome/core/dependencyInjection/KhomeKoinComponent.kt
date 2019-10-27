package khome.core.dependencyInjection

import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.Koin
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
abstract class KhomeKoinComponent : KoinComponent {
    override fun getKoin(): Koin = checkNotNull(KhomeKoinContext.application) { "No KoinApplication found" }.koin
}

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
inline fun <reified T> DefaultClientWebSocketSession.get(): T = object : KhomeKoinComponent() {}.get()
@KtorExperimentalAPI
@ObsoleteCoroutinesApi
inline fun <reified T> DefaultClientWebSocketSession.inject(): Lazy<T> = object : KhomeKoinComponent() {}.inject()

