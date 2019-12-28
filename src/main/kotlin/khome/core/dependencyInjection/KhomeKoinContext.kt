package khome.core.dependencyInjection

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import io.ktor.util.KtorExperimentalAPI
import khome.KhomeClient
import khome.core.ConfigurationInterface
import khome.core.DefaultConfiguration
import khome.core.ServiceStore
import khome.core.ServiceStoreInterface
import khome.core.StateStore
import khome.core.StateStoreInterface
import khome.core.eventHandling.Event
import khome.core.eventHandling.FailureResponseEvent
import khome.core.eventHandling.StateChangeEvent
import khome.core.logger
import khome.core.mapping.ObjectMapper
import khome.core.mapping.ObjectMapperInterface
import khome.core.mapping.OffsetDateTimeAdapter
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.time.OffsetDateTime
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
            single {
                GsonBuilder()
                    .setPrettyPrinting()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeAdapter().nullSafe())
                    .create()!!
            }
            single<ObjectMapperInterface> { ObjectMapper(get()) } bind ObjectMapper::class
            single<StateStoreInterface> { StateStore() }
            single<ServiceStoreInterface> { ServiceStore() }
            single { StateChangeEvent(Event()) }
            single { FailureResponseEvent(Event()) }
            single { newSingleThreadContext("ServiceContext") }
            single { AtomicInteger(0) }
            single<ConfigurationInterface> {
                DefaultConfiguration(
                    host = getProperty("HOST", "localhost"),
                    port = getProperty("PORT", 8123),
                    accessToken = getProperty("ACCESS_TOKEN", "<some-fancy-access-token>"),
                    secure = getProperty("SECURE", "false").toBoolean(),
                    startStateStream = getProperty("START_STATE_STREAM", "true").toBoolean(),
                    logLevel = getProperty("LOG_LEVEL", "INFO"),
                    logTime = getProperty("LOG_TIME", "true").toBoolean(),
                    logTimeFormat = getProperty("LOG_TIME_FORMAT", "yyyy-MM-dd HH:mm:ss"),
                    logOutput = getProperty("LOG_OUTPUT", "System.out")
                ).also { logger.debug { it } }
            }
            single { KhomeClient(get()) }
        }

    fun startKoinApplication() {
        application = koinApplication {
            environmentProperties()
            runCatching {
                fileProperties("/khome.properties")
            }.onFailure {
                logger.warn { "No khome.properties file found" }
            }

            modules(internalModule)
                .createEagerInstances()
        }
    }
}

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
fun loadKhomeModule(module: Module) =
    checkNotNull(KhomeKoinContext.application) { "Koin application not started yet" }
        .modules(module)
        .createEagerInstances()
