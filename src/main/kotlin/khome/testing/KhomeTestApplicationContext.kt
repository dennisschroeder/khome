package khome.testing

import io.ktor.util.KtorExperimentalAPI
import khome.KhomeClient
import khome.configureLogger
import khome.core.ServiceStoreInterface
import khome.core.StateStoreInterface
import khome.core.authenticate
import khome.core.dependencyInjection.KhomeTestComponent
import khome.core.dependencyInjection.loadKhomeModule
import khome.core.entities.AbstractEntity
import khome.fetchServices
import khome.fetchStates
import khome.storeServices
import khome.storeStates
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.module.Module
import org.koin.dsl.module

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
abstract class KhomeTestApplicationContext : KhomeTestComponent() {
    fun beans(declaration: Module.() -> Unit) =
        loadKhomeModule(module(override = true, moduleDeclaration = declaration))

    private val khomeClient: KhomeClient by inject()
    val stateStore: StateStoreInterface by inject()
    val serviceStore: ServiceStoreInterface by inject()

    fun getService(name: String) = serviceStore[name]
    fun <T> getState(entity: AbstractEntity<T>) = stateStore[entity.toString()]

    @BeforeAll
    fun startKhome() {
        runBlocking {
            khomeClient.startSession {
                configureLogger(get())
                authenticate(get())
                fetchStates(get())
                storeStates(consumeMessage(), get())
                fetchServices(get())
                storeServices(consumeMessage(), get())
            }
        }
    }
}
