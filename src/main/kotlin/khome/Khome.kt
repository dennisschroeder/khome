package khome

import io.ktor.util.KtorExperimentalAPI
import khome.core.ConfigurationInterface
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.dependencyInjection.KhomeKoinContext
import khome.core.dependencyInjection.KhomeModule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.get

/**
 * The main entry point to start your application
 *
 * @param init The type safe builder function to access the receiver
 * @return instance of Khome class instantiated with default values.
 */

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@KtorExperimentalAPI
fun khomeApplication(init: Khome.() -> Unit): KhomeApplication {
    KhomeKoinContext.startKoinApplication()
    val koinComponent = object : KhomeKoinComponent {}
    Khome(koinComponent.get()).apply(init)
    return koinComponent.get()
}

/**
 * The main application Class.
 * Serves all the tools necessary for the application to run.
 *
 * @author Dennis Schröder
 */
@ObsoleteCoroutinesApi
@KtorExperimentalAPI
class Khome(private val config: ConfigurationInterface) {
    companion object {
        var beanDeclarations: KhomeModule.() -> Unit = {}
    }

    /**
     * Configure your Khome instance. See all available properties in
     * the [ConfigurationInterface] data class.
     *
     * @param builder Lambda with receiver to configure Khome
     * @see [ConfigurationInterface]
     */
    fun configure(builder: ConfigurationInterface.() -> Unit) =
        config.apply(builder)

    fun beans(beanDeclarations: KhomeModule.() -> Unit) {
        Khome.beanDeclarations = beanDeclarations
    }
}
