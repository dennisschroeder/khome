package khome.core.dependencyInjection

import org.koin.core.get
import org.koin.core.Koin
import org.koin.core.KoinComponent

internal abstract class KhomeInternalKoinComponent : KoinComponent {
    override fun getKoin(): Koin = checkNotNull(KhomeInternalKoinContext.application) { "No KoinApplication found" }.koin
}

internal inline fun <reified T>internalRef():T = object : KhomeInternalKoinComponent() {}.get()
