package khome

import io.ktor.util.KtorExperimentalAPI
import khome.core.BaseKhomeComponent
import khome.core.authentication.Authenticator
import khome.core.boot.BootSequenceInterface
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.dependencyInjection.KhomeModulesInitializer
import khome.core.events.EventResponseConsumer
import khome.core.events.HassEventSubscriber
import khome.core.events.StateChangeEventSubscriber
import khome.core.servicestore.ServiceStoreInitializer
import khome.core.statestore.EntityStateInitializer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

@OptIn(
    ExperimentalStdlibApi::class,
    KtorExperimentalAPI::class,
    ObsoleteCoroutinesApi::class,
    ExperimentalCoroutinesApi::class
)
class KhomeApplication : KhomeKoinComponent {
    private val khomeClient: KhomeClient by inject()
    private val baseKhomeComponent: BaseKhomeComponent by inject()
    private val logger = KotlinLogging.logger { }

    suspend fun runApplication(listeners: suspend BaseKhomeComponent.() -> Unit = {}) =
        khomeClient.startSession {

            runBootSequence<Authenticator>(this)

            runBootSequence<ServiceStoreInitializer>(this)

            runBootSequence<KhomeModulesInitializer>(this)

            runBootSequence<EntityStateInitializer>(this)

            runBootSequence<HassEventSubscriber>(this)

            listeners(baseKhomeComponent)

            runBootSequence<StateChangeEventSubscriber>(this)

            runBootSequence<EventResponseConsumer>(this)
        }

    private suspend inline fun <reified BootSequence : BootSequenceInterface> runBootSequence(session: KhomeSession) =
        get<BootSequence> { parametersOf(session) }.runBootSequence()
}
