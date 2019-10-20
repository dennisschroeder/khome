package khome.core.dependencyInjection

import org.koin.core.KoinApplication
import org.koin.core.module.Module

/**
 * The Khome encapsulated public Koin context
 * https://insert-koin.io/docs/2.0/documentation/reference/index.html#_koin_context_isolation
 */

internal object KhomeInternalKoinContext {
    var application: KoinApplication? = null
    var internalModule: Module? = null
}
