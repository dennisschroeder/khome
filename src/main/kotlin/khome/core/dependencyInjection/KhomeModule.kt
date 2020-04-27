package khome.core.dependencyInjection

import io.ktor.util.KtorExperimentalAPI
import khome.core.events.HassEvent
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.definition.Definition
import org.koin.core.module.Module

class KhomeModule(val delegate: Module) {
    inline fun <reified T> bean(
        override: Boolean = false,
        noinline definition: Definition<T>
    ) = delegate.single(override = override, definition = definition)

    inline fun <reified T> service(
        override: Boolean = false,
        noinline definition: Definition<T>
    ) = delegate.factory(override = override, definition = definition)

    inline fun <reified T : HassEvent> event(
        noinline definition: Definition<T>
    ) {
        delegate.single(override = false, definition = definition)
    }
}

fun khomeModule(
    createdAtStart: Boolean = false,
    override: Boolean = false,
    moduleDeclaration: KhomeModule.() -> Unit
): KhomeModule {
    val module = Module(createdAtStart, override)
    val khomeModule = KhomeModule(module)
    moduleDeclaration(khomeModule)
    return khomeModule
}

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
fun loadKhomeModule(module: KhomeModule) =
    checkNotNull(KhomeKoinContext.application) { "Koin application not started yet" }
        .modules(module.delegate)
        .createEagerInstances()
