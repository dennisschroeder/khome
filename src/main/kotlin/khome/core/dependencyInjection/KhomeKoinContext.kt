package khome.core.dependencyInjection

import io.ktor.util.KtorExperimentalAPI
import khome.KhomeClient
import khome.core.ConfigurationInterface
import khome.core.DefaultConfiguration
import khome.core.ServiceStore
import khome.core.StateStore
import khome.core.eventHandling.FailureResponseEvents
import khome.core.eventHandling.StateChangeEvents
import khome.core.eventHandling.SuccessResponseEvents
import khome.core.logger
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import org.koin.core.KoinApplication
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicInteger

internal typealias ServiceCoroutineContext = ExecutorCoroutineDispatcher
internal typealias CallerID = AtomicInteger

/**
 * The Khome encapsulated public Koin context
 * @link https://insert-koin.io/docs/2.0/documentation/reference/index.html#_koin_context_isolation
 */

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
object KhomeKoinContext {
    var application: KoinApplication? = null

    private var internalModule: Module =
        module {
            single { StateStore() }
            single { ServiceStore() }
            single { StateChangeEvents() }
            single { SuccessResponseEvents() }
            single { FailureResponseEvents() }
            single { newSingleThreadContext("ServiceContext") }
            single { AtomicInteger(0) }
            single<ConfigurationInterface> { DefaultConfiguration() }
            single { KhomeClient(get()) }
        }

    fun startKoinApplication() {
        application = koinApplication {
            printLogger(Level.DEBUG)
            environmentProperties()
            runCatching {
                fileProperties("/khome.properties")
            }.onFailure {
                logger.warn { "No khome.properties file found" }
            }

            modules(internalModule)
        }.createEagerInstances()
    }
}

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
fun loadKhomeModule(module: Module) =
    checkNotNull(KhomeKoinContext.application) { "Koin application not started yet" }.modules(module)
