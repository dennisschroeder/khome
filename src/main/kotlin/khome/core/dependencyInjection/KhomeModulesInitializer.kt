package khome.core.dependencyInjection

import io.ktor.util.KtorExperimentalAPI
import khome.DefaultErrorResponseObserver
import khome.ErrorResponseHandlerInterface
import khome.HassApi
import khome.Khome
import khome.KhomeSession
import khome.calling.PersistentNotificationCreate
import khome.core.ConfigurationInterface
import khome.core.boot.BootSequenceInterface
import khome.core.entities.EntityIdToEntityTypeMap
import khome.core.entities.system.DateTime
import khome.core.entities.system.Sun
import khome.core.entities.system.Time
import khome.core.events.DefaultErrorResultListenerExceptionHandler
import khome.core.events.DefaultHassEventListenerExceptionHandler
import khome.core.events.DefaultEntityObserverExceptionHandler
import khome.core.events.ErrorResultListenerExceptionHandler
import khome.core.events.EventListenerExceptionHandler
import khome.core.events.EntityObserverExceptionHandler
import kotlinx.coroutines.ObsoleteCoroutinesApi

@OptIn(ObsoleteCoroutinesApi::class, ExperimentalStdlibApi::class, KtorExperimentalAPI::class)
internal class KhomeModulesInitializer(
    override val khomeSession: KhomeSession,
    private val configuration: ConfigurationInterface
) :
    BootSequenceInterface {

    private val systemBeansModule =
        khomeModule(createdAtStart = true, override = true) {
            bean { HassApi(khomeSession, get(), get(), get()) }
            bean { EntityIdToEntityTypeMap(hashMapOf()) }
            bean { Sun() }
            bean { Time() }
            bean { DateTime() }
            service { PersistentNotificationCreate() }
            if (configuration.enableDefaultErrorResponseHandler)
                bean<ErrorResponseHandlerInterface> { DefaultErrorResponseObserver(get(), get()) }
            if (configuration.enableDefaultStateChangeListenerExceptionHandler)
                bean<EntityObserverExceptionHandler> { DefaultEntityObserverExceptionHandler(get(), get()) }
            if (configuration.enableHassEventListenerExceptionHandler)
                bean<EventListenerExceptionHandler> { DefaultHassEventListenerExceptionHandler(get(), get()) }
            if (configuration.enableErrorResponseListenerExceptionHandler)
                bean<ErrorResultListenerExceptionHandler> { DefaultErrorResultListenerExceptionHandler(get(), get()) }
        }

    private val userBeansModule =
        khomeModule(
            createdAtStart = true,
            moduleDeclaration = Khome.beanDeclarations
        )

    override suspend fun runBootSequence() {
        loadKhomeModule(systemBeansModule)
        loadKhomeModule(userBeansModule)
    }
}
