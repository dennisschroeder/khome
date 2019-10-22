package khome.core.dependencyInjection

import org.koin.core.KoinApplication
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.lang.IllegalStateException

/**
 * The Khome encapsulated public Koin context
 * https://insert-koin.io/docs/2.0/documentation/reference/index.html#_koin_context_isolation
 */

internal object KhomePublicKoinContext {
    var application: KoinApplication? = null
    var publicModule: Module? = null

    fun declareModule(beanDeclarations: Module.() -> Unit) = module(
        moduleDeclaration = beanDeclarations
    ).let { publicModule = it }

    fun startKoinApplication() {
        application = koinApplication {
            printLogger(Level.DEBUG)
            publicModule?.let { modules(it) }
                ?: throw IllegalStateException("Public module not initiated")
        }
    }
}
