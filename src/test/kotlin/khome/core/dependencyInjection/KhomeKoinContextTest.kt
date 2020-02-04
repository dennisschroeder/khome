package khome.core.dependencyInjection

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isSuccess
import assertk.assertions.isTrue
import com.google.gson.Gson
import khome.KhomeClient
import khome.core.ConfigurationInterface
import khome.core.DefaultConfiguration
import khome.core.ServiceStore
import khome.core.ServiceStoreInterface
import khome.core.StateStore
import khome.core.StateStoreInterface
import khome.core.eventHandling.StateChangeEvent
import khome.core.mapping.ObjectMapper
import khome.core.mapping.ObjectMapperInterface
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.core.get
import org.koin.dsl.bind
import org.koin.test.check.checkModules
import java.util.concurrent.atomic.AtomicInteger

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class KhomeKoinContextTest : KhomeComponent() {

    private val sut = KhomeKoinContext

    @BeforeEach
    fun init() {
        sut.startKoinApplication()
    }

    @Test
    fun `assert khome components hierarchy is correct`() {
        assertThat {
            sut.application?.let {
                it.checkModules()
            }
        }.isSuccess()
    }

    @Test
    fun `assert all khome components are loaded and can be fetched`() {
        assertThat {
            val gson: Gson = get()

            val stateStore: StateStoreInterface = get()
            assertThat(stateStore).isInstanceOf(StateStore::class)

            val serviceStore: ServiceStoreInterface = get()
            assertThat(serviceStore).isInstanceOf(ServiceStore::class)

            val objectMapperInterface: ObjectMapperInterface = get()
            val objectMapperImplementation: ObjectMapper = get()

            val stateChangeEvent: StateChangeEvent = get()
            val failureResponseEvent: StateChangeEvent = get()

            val serviceCoroutineContext: ServiceCoroutineContext = get()
            assertThat(serviceCoroutineContext).isInstanceOf(ExecutorCoroutineDispatcher::class)

            val callerID: CallerID = get()
            assertThat(callerID).isInstanceOf(AtomicInteger::class)

            val configuration: ConfigurationInterface = get()
            assertThat(configuration).isInstanceOf(DefaultConfiguration::class)

            val khomeClient: KhomeClient = get()
        }.isSuccess()
    }

    @Test
    fun `assert configuration bean can be overwritten`() {
        data class CustomConfig(
            override var host: String = "somehost.com",
            override var port: Int = 1234,
            override var accessToken: String = "some-super-secret-token",
            override var secure: Boolean = true,
            override var startStateStream: Boolean = true
        ) : ConfigurationInterface

        val testModule = khomeModule {
            bean<ConfigurationInterface>(override = true) { CustomConfig() } bind CustomConfig::class
        }

        loadKhomeModule(testModule)

        val customConfig: CustomConfig = get()
        assertThat(customConfig).isDataClassEqualTo(CustomConfig())
    }

    @Test
    fun `assert environment variables are loaded via koin in configuration`() {
        val config: ConfigurationInterface = get()

        assertThat(config.host).isEqualTo("home-assistant.local")
        assertThat(config.port).isEqualTo(8321)
        assertThat(config.accessToken).isEqualTo("dsq7zht54899dhz43kbv4dgr56a8we234h>!sg?x")
        assertThat(config.secure).isTrue()
        assertThat(config.startStateStream).isFalse()
    }

    @AfterEach
    fun close() {
        sut.application?.let { it.close() }
    }
}
