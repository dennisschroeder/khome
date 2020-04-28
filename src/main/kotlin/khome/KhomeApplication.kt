package khome

import io.ktor.util.KtorExperimentalAPI
import khome.core.BaseKhomeComponent
import khome.core.authentication.Authenticator
import khome.core.dependencyInjection.KhomeModulesInitializer
import khome.core.events.EventResponseConsumer
import khome.core.events.HassEventSubscriber
import khome.core.events.StateChangeEventSubscriber
import khome.core.servicestore.ServiceStoreInitializer
import khome.core.statestore.StateStoreInitializer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.coroutineScope

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
@ObsoleteCoroutinesApi
class KhomeApplication(
    private val khomeClient: KhomeClient,
    private val baseKhomeComponent: BaseKhomeComponent
) {
    @InternalCoroutinesApi
    suspend fun runApplication(listeners: suspend BaseKhomeComponent.() -> Unit = {}) =
        coroutineScope {
            khomeClient.startSession {

                runBootSequence<Authenticator>()

                runBootSequence<ServiceStoreInitializer>()

                runBootSequence<StateStoreInitializer>()

                runBootSequence<KhomeModulesInitializer>()

                runBootSequence<HassEventSubscriber>()

                listeners(baseKhomeComponent)

                runBootSequence<StateChangeEventSubscriber>()

                runBootSequence<EventResponseConsumer>()
            }
        }
}
