package khome.core.dependencyInjection

import org.koin.core.get
import org.koin.core.Koin
import org.koin.core.KoinComponent

abstract class KhomePublicKoinComponent : KoinComponent {
    override fun getKoin(): Koin = checkNotNull(KhomePublicKoinContext.application) { "No KoinApplication found" }.koin
}

inline fun <reified T> ref(): T = object : KhomePublicKoinComponent() {}.get()
