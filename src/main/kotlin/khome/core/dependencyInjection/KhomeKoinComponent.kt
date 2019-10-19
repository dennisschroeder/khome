package khome.core.dependencyInjection

import khome.Khome
import org.koin.core.get
import org.koin.core.Koin
import org.koin.core.KoinComponent

abstract class KhomeKoinComponent : KoinComponent {
    override fun getKoin(): Koin = Khome.koinApp?.koin!!
}

inline fun <reified T>ref():T = object : KhomeKoinComponent() {}.get()
