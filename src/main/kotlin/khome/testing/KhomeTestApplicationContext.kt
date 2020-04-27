package khome.testing

import io.ktor.util.KtorExperimentalAPI
import khome.KhomeClient
import khome.core.servicestore.ServiceStoreInterface
import khome.core.statestore.StateStoreInterface
import khome.core.authentication.Authenticator
import khome.core.dependencyInjection.KhomeModule
import khome.core.dependencyInjection.KhomeTestComponent
import khome.core.dependencyInjection.khomeModule
import khome.core.dependencyInjection.loadKhomeModule
import khome.core.entities.AbstractEntity
import khome.core.servicestore.ServiceStoreInitializer
import khome.core.statestore.StateStoreInitializer
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
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
                runBootSequence<Authenticator>()
                runBootSequence<ServiceStoreInitializer>()
                runBootSequence<StateStoreInitializer>()
            }
        }
    }
}
