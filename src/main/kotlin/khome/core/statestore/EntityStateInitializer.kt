package khome.core.statestore

import io.ktor.util.KtorExperimentalAPI
import khome.KhomeSession
import khome.core.boot.BootSequenceInterface
import khome.core.dependencyInjection.CallerID
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.entities.EntityIdToEntityTypeMap
import khome.core.entities.EntitySubject
import khome.core.entities.exceptions.EntityNotFoundException
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging

@OptIn(ObsoleteCoroutinesApi::class, KtorExperimentalAPI::class, ExperimentalStdlibApi::class)
internal class EntityStateInitializer(
    override val khomeSession: KhomeSession,
    private val callerID: CallerID,
    private val entityIdToEntityTypeMap: EntityIdToEntityTypeMap
) : BootSequenceInterface, KhomeKoinComponent {

    private val logger = KotlinLogging.logger { }
    private val id
        get() = callerID.incrementAndGet()

    private val statesRequest = StatesRequest(id)
    private val listOfEntityIds =
        entityIdToEntityTypeMap
            .map { entry -> entry.key }
            .toMutableList()

    override suspend fun runBootSequence() {
        sendStatesRequest()
        logger.info { "Requested initial entity states" }
        setInitialEntityState(consumeStatesResponse())
        runEntityHealthCheck()
    }

    private suspend fun sendStatesRequest() =
        khomeSession.callWebSocketApi(statesRequest)

    private suspend fun consumeStatesResponse() =
        khomeSession.consumeSingleMessage<StatesResponse>()

    private fun setInitialEntityState(statesResponse: StatesResponse) =
        when (statesResponse.success) {
            false -> logger.error { "Could not fetch initial states from homeassistant" }
            true -> {
                statesResponse.result.forEach { state ->
                    getEntityOrNull(state.entityId)?.let { entity ->
                        entity._state = state
                        logger.debug { "Set initial state for entity: ${state.entityId} with: $state" }
                    }
                }
                logger.info { "All initial entity states are set." }
            }
        }

    private fun getEntityOrNull(entityId: String) =
        entityIdToEntityTypeMap[entityId]?.let { clazz ->
            listOfEntityIds.remove(entityId)
            getKoin().get<EntitySubject<*>>(clazz, null, null)
        }

    private fun runEntityHealthCheck() {
        if (listOfEntityIds.size > 0)
            throw EntityNotFoundException("Could not found ${listOfEntityIds.joinToString(",")}. Check your entity classes.")
    }
}
