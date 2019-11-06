package khome.testing

import io.ktor.util.KtorExperimentalAPI
import khome.KhomeClient
import khome.consumeMessage
import khome.core.StateStoreInterface
import khome.core.authenticate
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.dependencyInjection.KhomeKoinContext
import khome.core.dependencyInjection.get
import khome.core.dependencyInjection.loadKhomeModule
import khome.fetchStates
import khome.storeStates
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.koin.core.inject
import org.koin.core.module.Module
import org.koin.dsl.module

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
abstract class KhomeTest : KhomeKoinComponent() {

    init {
        KhomeKoinContext.startKoinApplication()
    }

    fun beans(declaration: Module.() -> Unit) = loadKhomeModule(module(moduleDeclaration = declaration))

    private val khomeClient: KhomeClient by inject()
    val stateStore: StateStoreInterface by inject()

    @BeforeAll
    fun startKhome() {
        runBlocking {
            khomeClient.startSession {
                authenticate(get())
                fetchStates(get())
                storeStates(consumeMessage(), get())
            }
        }
    }
}
