package khome

import io.ktor.util.KtorExperimentalAPI
import khome.core.ConfigurationInterface
import khome.core.koin.KhomeComponent
import khome.core.koin.KhomeKoinContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.inject

typealias KhomeBuilder = Khome.() -> Unit

/**
 * The main entry point to start your application
 *
 * @param init The type safe builder function to access the receiver
 * @return instance of Khome class instantiated with default values.
 */

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@KtorExperimentalAPI
fun khomeApplication(init: KhomeBuilder = {}): KhomeApplication =
    KhomeImpl().apply(init).createApplication()

/**
 * The main application interface.
 * Serves all the tools necessary for the application to run.
 *
 * @author Dennis Schröder
 */
interface Khome {
    /**
     * Configure your Khome instance. See all available properties in
     * the [ConfigurationInterface] data class.
     *
     * @param builder Lambda with receiver to configure Khome
     */
    fun configure(builder: ConfigurationInterface.() -> Unit): ConfigurationInterface
}

@OptIn(ExperimentalStdlibApi::class, KtorExperimentalAPI::class, ObsoleteCoroutinesApi::class)
private class KhomeImpl : Khome, KhomeComponent {

    init {
        KhomeKoinContext.startKoinApplication()
    }

    private val config: ConfigurationInterface by inject()

    override fun configure(builder: ConfigurationInterface.() -> Unit) =
        config.apply(builder)

    internal fun createApplication() = KhomeApplicationImpl()
}
