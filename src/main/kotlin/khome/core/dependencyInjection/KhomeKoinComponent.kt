package khome.core.dependencyInjection

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.Koin
import org.koin.core.KoinComponent

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
abstract class KhomeKoinComponent : KoinComponent {
    override fun getKoin(): Koin = checkNotNull(KhomeKoinContext.application) { "No KoinApplication found" }.koin
}
