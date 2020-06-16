package khome

import io.ktor.util.KtorExperimentalAPI
import khome.core.Configuration
import khome.core.koin.KhomeComponent
import khome.core.koin.KhomeKoinContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.inject

/**
 * The lambda with [Khome] as receiver to configure Khome
 */
typealias KhomeBuilder = Khome.() -> Unit

/**
 * The main entry point to start your application
 *
 * @param init The type safe builder function to access the receiver
 * @return [KhomeApplication]
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
     * the [Configuration] data class.
     *
     * @param builder Lambda with [Configuration] receiver to configure Khome.
     */
    fun configure(builder: Configuration.() -> Unit): Configuration
}

@OptIn(ExperimentalStdlibApi::class, KtorExperimentalAPI::class, ObsoleteCoroutinesApi::class)
private class KhomeImpl : Khome, KhomeComponent {

    init {
        KhomeKoinContext.startKoinApplication()
    }

    private val config: Configuration by inject()

    override fun configure(builder: Configuration.() -> Unit) =
        config.apply(builder)

    internal fun createApplication() = KhomeApplicationImpl()
}
