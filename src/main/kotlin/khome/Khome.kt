package khome

import io.ktor.util.KtorExperimentalAPI
import khome.core.ConfigurationInterface
import khome.core.koin.KhomeKoinComponent
import khome.core.koin.KhomeKoinContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.inject

/**
 * The main entry point to start your application
 *
 * @param init The type safe builder function to access the receiver
 * @return instance of Khome class instantiated with default values.
 */

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@KtorExperimentalAPI
fun khomeApplication(init: Khome.() -> Unit): KhomeApplicationImpl {
    val khome = Khome()
    khome.apply(init)
    return khome.createApplication()
}

/**
 * The main application Class.
 * Serves all the tools necessary for the application to run.
 *
 * @author Dennis Schröder
 */
@OptIn(ExperimentalStdlibApi::class, KtorExperimentalAPI::class, ObsoleteCoroutinesApi::class)
class Khome : KhomeKoinComponent {

    init {
        KhomeKoinContext.startKoinApplication()
    }

    private val config: ConfigurationInterface by inject()

    /**
     * Configure your Khome instance. See all available properties in
     * the [ConfigurationInterface] data class.
     *
     * @param builder Lambda with receiver to configure Khome
     * @see [ConfigurationInterface]
     */
    fun configure(builder: ConfigurationInterface.() -> Unit) =
        config.apply(builder)

    internal fun createApplication() = KhomeApplicationImpl()
}
