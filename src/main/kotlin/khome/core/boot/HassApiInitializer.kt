package khome.core.boot

import io.ktor.util.KtorExperimentalAPI
import khome.communicating.HassApi
import khome.KhomeSession
import khome.core.koin.KhomeKoinContext
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.dsl.module

@OptIn(ObsoleteCoroutinesApi::class, ExperimentalStdlibApi::class, KtorExperimentalAPI::class)
internal class HassApiInitializer(
    override val khomeSession: KhomeSession
) : BootSequenceInterface {

    private val systemBeansModule =
        module {
            single { HassApi(khomeSession, get(), get()) }
        }

    override suspend fun runBootSequence() {
        KhomeKoinContext.application?.modules(systemBeansModule)
    }
}
