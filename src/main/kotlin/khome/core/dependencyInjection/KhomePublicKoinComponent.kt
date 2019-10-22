package khome.core.dependencyInjection

import org.koin.core.get
import org.koin.core.Koin
import org.koin.core.KoinComponent
import org.koin.core.inject
import io.ktor.client.features.websocket.DefaultClientWebSocketSession

abstract class KhomePublicKoinComponent : KoinComponent {
    override fun getKoin(): Koin = checkNotNull(KhomePublicKoinContext.application) { "No KoinApplication found" }.koin
}

inline fun <reified T> DefaultClientWebSocketSession.get(): T = object : KhomePublicKoinComponent() {}.get()
inline fun <reified T> DefaultClientWebSocketSession.inject(): Lazy<T> = object : KhomePublicKoinComponent() {}.inject()
