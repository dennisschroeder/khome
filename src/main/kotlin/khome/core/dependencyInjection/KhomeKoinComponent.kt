package khome.core.dependencyInjection

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.Koin
import org.koin.core.KoinComponent

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
abstract class KhomeKoinComponent : KoinComponent {
    override fun getKoin(): Koin = checkNotNull(KhomeKoinContext.application) { "No KoinApplication found" }.koin
}

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
abstract class KhomeTestComponent : KhomeKoinComponent() {

    @BeforeEach
    fun initKoin() {
        KhomeKoinContext.startKoinApplication()
    }

    @AfterEach
    fun closeKoinApplication() {
        KhomeKoinContext.application?.let { it.close() }
    }
}
