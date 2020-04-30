package khome.core.dependencyInjection

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.client.request.host
import io.ktor.client.request.port
import io.ktor.util.KtorExperimentalAPI
import khome.KhomeApplication
import khome.KhomeClient
import khome.KhomeSession
import khome.calling.ServiceCoroutineContext
import khome.core.BaseKhomeComponent
import khome.core.ConfigurationInterface
import khome.core.DefaultConfiguration
import khome.core.authentication.Authenticator
import khome.core.clients.RestApiClient
import khome.core.clients.WebSocketClient
import khome.core.entities.EntityInterface
import khome.core.events.ErrorResponseEvent
import khome.core.events.Event
import khome.core.events.EventResponseConsumer
import khome.core.events.HassEventRegistry
import khome.core.events.HassEventSubscriber
import khome.core.events.StateChangeEvent
import khome.core.events.StateChangeEventSubscriber
import khome.core.mapping.KhomeEntityConverter
import khome.core.mapping.ObjectMapper
import khome.core.mapping.ObjectMapperInterface
import khome.core.mapping.OffsetDateTimeAdapter
import khome.core.servicestore.ServiceStore
import khome.core.servicestore.ServiceStoreInitializer
import khome.core.servicestore.ServiceStoreInterface
import khome.core.statestore.StateStore
import khome.core.statestore.StateStoreInitializer
import khome.core.statestore.StateStoreInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import mu.KotlinLogging
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.time.OffsetDateTime
import java.util.concurrent.atomic.AtomicInteger

internal typealias CallerID = AtomicInteger

/**
 * The Khome encapsulated public Koin context
 * @link https://insert-koin.io/docs/2.0/documentation/reference/index.html#_koin_context_isolation
 */

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
object KhomeKoinContext {
    private const val NAME = "NAME"
    private const val HOST = "HOST"
    private const val PORT = "PORT"
    private const val ACCESS_TOKEN = "ACCESS_TOKEN"
    private const val SECURE = "SECURE"
    private const val ENABLE_DEFAULT_ERROR_RESPONSE_HANDLER = "ENABLE_DEFAULT_ERROR_RESPONSE_HANDLER"
    private const val ENABLE_DEFAULT_STATE_CHANGE_LISTENER_EXCEPTION_HANDLER =
        "ENABLE_DEFAULT_STATE_CHANGE_LISTENER_EXCEPTION_HANDLER"
    private const val ENABLE_DEFAULT_HASS_EVENT_LISTENER_EXCEPTION_HANDLER =
        "ENABLE_DEFAULT_HASS_EVENT_LISTENER_EXCEPTION_HANDLER"
    private const val ENABLE_DEFAULT_ERROR_RESPONSE_LISTENER_EXCEPTION_HANDLER =
        "ENABLE_DEFAULT_ERROR_RESPONSE_LISTENER_EXCEPTION_HANDLER"

    var application: KoinApplication? = null
    private val logger = KotlinLogging.logger {}

    @ExperimentalCoroutinesApi
    private var internalModule: Module =
        module {

            single {
                GsonBuilder()
                    .setPrettyPrinting()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeAdapter().nullSafe())
                    .registerTypeAdapter(EntityInterface::class.java, KhomeEntityConverter().nullSafe())
                    .create()!!
            }
            single<ObjectMapperInterface> { ObjectMapper(get()) } bind ObjectMapper::class
            single<StateStoreInterface> { StateStore() }
            single<ServiceStoreInterface> { ServiceStore() }
            single { StateChangeEvent(Event()) }
            single { ErrorResponseEvent(Event()) }
            single { HassEventRegistry() }
            single { ServiceCoroutineContext(newSingleThreadContext("ServiceContext")) }
            single { AtomicInteger(0) }
            single<ConfigurationInterface> {
                DefaultConfiguration(
                    name = getProperty(NAME, "[Give your application a unique name]"),
                    host = getProperty(HOST, "localhost"),
                    port = getProperty(PORT, 8123),
                    accessToken = getProperty(ACCESS_TOKEN, "<some-fancy-access-token>"),
                    secure = getProperty(SECURE, "false").toBoolean(),
                    enableDefaultErrorResponseHandler = getProperty(
                        ENABLE_DEFAULT_ERROR_RESPONSE_HANDLER,
                        "true"
                    ).toBoolean(),
                    enableDefaultStateChangeListenerExceptionHandler = getProperty(
                        ENABLE_DEFAULT_STATE_CHANGE_LISTENER_EXCEPTION_HANDLER,
                        "true"
                    ).toBoolean(),
                    enableHassEventListenerExceptionHandler = getProperty(
                        ENABLE_DEFAULT_HASS_EVENT_LISTENER_EXCEPTION_HANDLER,
                        "true"
                    ).toBoolean(),
                    enableErrorResponseListenerExceptionHandler = getProperty(
                        ENABLE_DEFAULT_ERROR_RESPONSE_LISTENER_EXCEPTION_HANDLER,
                        "true"
                    ).toBoolean()
                )
            }
            single {
                val client = HttpClient(CIO).config { install(WebSockets) }
                WebSocketClient(client)
            }
            single {
                val client = HttpClient(CIO) {
                    install(JsonFeature) {
                        serializer = GsonSerializer {
                            setPrettyPrinting()
                            setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                            registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeAdapter().nullSafe())
                            create()!!
                        }
                    }

                    val config = get<ConfigurationInterface>()

                    defaultRequest {
                        host = config.host
                        port = config.port
                        header("Authorization", "Bearer ${config.accessToken}")
                        header("Content-Type", "application/json")
                    }
                }
                RestApiClient(client)
            }

            single { (websocketSession: DefaultClientWebSocketSession) -> KhomeSession(websocketSession) }

            single { (khomeSession: KhomeSession) ->
                Authenticator(
                    khomeSession = khomeSession,
                    configuration = get()
                )
            }
            single { (khomeSession: KhomeSession) ->
                ServiceStoreInitializer(
                    khomeSession = khomeSession,
                    callerID = get(),
                    serviceStore = get()
                )
            }
            single { (khomeSession: KhomeSession) ->
                StateStoreInitializer(
                    khomeSession = khomeSession,
                    callerID = get(),
                    stateStore = get()
                )
            }

            single { (khomeSession: KhomeSession) ->
                HassEventSubscriber(
                    khomeSession = khomeSession,
                    callerID = get(),
                    registry = get()
                )
            }

            single { (khomeSession: KhomeSession) ->
                StateChangeEventSubscriber(
                    khomeSession = khomeSession,
                    callerID = get()
                )
            }

            single { (khomeSession: KhomeSession) ->
                EventResponseConsumer(
                    khomeSession = khomeSession,
                    stateChangeEvent = get(),
                    objectMapper = get(),
                    stateStore = get(),
                    hassEventRegistry = get(),
                    errorResponseEvent = get()
                )
            }

            single { (khomeSession: KhomeSession) ->
                KhomeModulesInitializer(
                    khomeSession = khomeSession,
                    configuration = get()
                )
            }
            single { KhomeClient(get(), get()) }
            single { BaseKhomeComponent() }
            single { KhomeApplication(get(), get()) }
        }

    @ExperimentalCoroutinesApi
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
