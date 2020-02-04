package khome.testing

import io.ktor.util.KtorExperimentalAPI
import khome.KhomeClient
import khome.core.ServiceStoreInterface
import khome.core.StateStoreInterface
import khome.core.authenticate
import khome.core.dependencyInjection.KhomeModule
import khome.core.dependencyInjection.KhomeTestComponent
import khome.core.dependencyInjection.khomeModule
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

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
abstract class KhomeTestApplicationContext : KhomeTestComponent() {
    fun beans(declaration: KhomeModule.() -> Unit) =
        loadKhomeModule(khomeModule(override = true, createdAtStart = true, moduleDeclaration = declaration))

    private val khomeClient: KhomeClient by inject()
    private val stateStore: StateStoreInterface by inject()
    private val serviceStore: ServiceStoreInterface by inject()

    fun getService(name: String) = serviceStore[name]
    fun <T> getState(entity: AbstractEntity<T>) = stateStore[entity.toString()]

    @BeforeAll
    fun startKhome() {
        runBlocking {
            khomeClient.startSession {
                authenticate(get())
                fetchStates(get())
                storeStates(consumeMessage(), get())
                fetchServices(get())
                storeServices(consumeMessage(), get())
            }
        }
    }
}
