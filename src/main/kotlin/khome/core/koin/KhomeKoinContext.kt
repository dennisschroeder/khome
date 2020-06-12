@file:Suppress("EXPERIMENTAL_API_USAGE")

package khome.core.koin

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.client.request.host
import io.ktor.client.request.port
import khome.HassClient
import khome.KhomeApplicationImpl
import khome.core.Configuration
import khome.core.DefaultConfiguration
import khome.core.boot.servicestore.ServiceStore
import khome.core.boot.servicestore.ServiceStoreInterface
import khome.core.clients.RestApiClient
import khome.core.clients.WebSocketClient
import khome.core.mapping.EntityIdAdapter
import khome.core.mapping.InstantAdapter
import khome.core.mapping.LocalDateAdapter
import khome.core.mapping.LocalDateTimeAdapter
import khome.core.mapping.LocalTimeAdapter
import khome.core.mapping.ObjectMapper
import khome.core.mapping.ObjectMapperInterface
import khome.core.mapping.OffsetDateTimeAdapter
import khome.entities.EntityId
import mu.KotlinLogging
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime

/**
 * The Khome encapsulated public Koin context
 * @link https://insert-koin.io/docs/2.0/documentation/reference/index.html#_koin_context_isolation
 */

internal object KhomeKoinContext {
    private const val NAME = "NAME"
    private const val HOST = "HOST"
    private const val PORT = "PORT"
    private const val ACCESS_TOKEN = "ACCESS_TOKEN"
    private const val SECURE = "SECURE"

    var application: KoinApplication? = null
    private val logger = KotlinLogging.logger {}

    private var internalModule: Module =
        module {

            single {
                GsonBuilder()
                    .setPrettyPrinting()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeAdapter().nullSafe())
                    .registerTypeAdapter(Instant::class.java, InstantAdapter().nullSafe())
                    .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter().nullSafe())
                    .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter().nullSafe())
                    .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter().nullSafe())
                    .registerTypeAdapter(EntityId::class.java, EntityIdAdapter().nullSafe())
                    .create()!!
            }
            single<ObjectMapperInterface> { ObjectMapper(get()) } bind ObjectMapper::class
            single<ServiceStoreInterface> { ServiceStore() }
            single<Configuration> {
                DefaultConfiguration(
                    name = getProperty(NAME, "Khome"),
                    host = getProperty(HOST, "localhost"),
                    port = getProperty(PORT, 8123),
                    accessToken = getProperty(ACCESS_TOKEN, "<some-fancy-access-token>"),
                    secure = getProperty(SECURE, "false").toBoolean()
                )
            }
            single {
                val client = HttpClient(CIO) {
                    install(JsonFeature) {
                        serializer = GsonSerializer {
                            setPrettyPrinting()
                            setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                            registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeAdapter().nullSafe())
                            registerTypeAdapter(EntityId::class.java, EntityIdAdapter().nullSafe())
                            registerTypeAdapter(LocalDate::class.java, LocalDateAdapter().nullSafe())
                            registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter().nullSafe())
                            create()!!
                        }
                    }

                    val config = get<Configuration>()

                    defaultRequest {
                        host = config.host
                        port = config.port
                        header("Authorization", "Bearer ${config.accessToken}")
                        header("Content-Type", "application/json")
                    }
                }
                RestApiClient(client)
            }
            single { HassClient(get(), WebSocketClient(HttpClient(CIO).config { install(WebSockets) })) }
            single { KhomeApplicationImpl() }
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
