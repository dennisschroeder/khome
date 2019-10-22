package khome.core.dependencyInjection

import io.ktor.util.KtorExperimentalAPI
import khome.KhomeClient
import khome.core.Configuration
import khome.core.ConfigurationInterface
import khome.core.ServiceStore
import khome.core.StateStore
import khome.core.eventHandling.FailureResponseEvents
import khome.core.eventHandling.StateChangeEvents
import khome.core.eventHandling.SuccessResponseEvents
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import org.koin.core.KoinApplication
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicInteger


internal typealias ServiceContext = ExecutorCoroutineDispatcher
internal typealias CallerID = AtomicInteger

/**
 * The Khome encapsulated public Koin context
 * https://insert-koin.io/docs/2.0/documentation/reference/index.html#_koin_context_isolation
 */

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
internal object KhomeInternalKoinContext {
    var application: KoinApplication? = null

    private var internalModule: Module = module(createdAtStart = true) {
        single { StateStore() }
        single { ServiceStore() }
        single { StateChangeEvents() }
        single { SuccessResponseEvents() }
        single { FailureResponseEvents() }
        single { newSingleThreadContext("ServiceContext") }
        single { AtomicInteger(0) }
        single<ConfigurationInterface>(override = true) { Configuration() }
        single { KhomeClient(get()) }
    }

    fun startKoinApplication() {
        application = koinApplication {
            printLogger(Level.DEBUG)
            modules(listOf(internalModule))
        }
    }
}
