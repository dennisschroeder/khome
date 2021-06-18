package khome.core.boot.statehandling

import com.google.gson.JsonObject
import io.ktor.util.KtorExperimentalAPI
import khome.KhomeSession
import khome.communicating.CALLER_ID
import khome.core.koin.KhomeComponent
import khome.entities.ActuatorStateUpdater
import khome.entities.EntityRegistrationValidation
import khome.entities.SensorStateUpdater
import khome.values.EntityId
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging

interface EntityStateInitializer {
    suspend fun initialize()
}

@OptIn(ObsoleteCoroutinesApi::class, KtorExperimentalAPI::class)
internal class EntityStateInitializerImpl(
    val khomeSession: KhomeSession,
    private val sensorStateUpdater: SensorStateUpdater,
    private val actuatorStateUpdater: ActuatorStateUpdater,
    private val entityRegistrationValidation: EntityRegistrationValidation
) : EntityStateInitializer, KhomeComponent {

    private val logger = KotlinLogging.logger { }
    private val id
        get() = CALLER_ID.incrementAndGet()

    private val statesRequest = StatesRequest(id)

    @ExperimentalStdlibApi
    override suspend fun initialize() {
        sendStatesRequest()
        logger.info { "Requested initial entity states" }
        setInitialEntityState(consumeStatesResponse())
    }

    private suspend fun sendStatesRequest() =
        khomeSession.callWebSocketApi(statesRequest)

    private suspend fun consumeStatesResponse() =
        khomeSession.consumeSingleMessage<StatesResponse>()

    @ExperimentalStdlibApi
    private fun setInitialEntityState(stateResponse: StatesResponse) {
        if (stateResponse.success) {
            val statesByEntityId = stateResponse.result.associateBy { state ->
                khomeSession.objectMapper.fromJson(state["entity_id"], EntityId::class.java)
            }
            entityRegistrationValidation.validate(statesByEntityId.map { it.key })
            for (state in statesByEntityId) {
                sensorStateUpdater(flattenStateAttributes(state.value), state.key)
                actuatorStateUpdater(flattenStateAttributes(state.value), state.key)
            }
        }
    }
}

internal fun flattenStateAttributes(stateResponse: JsonObject): JsonObject {
    val attributesAsJsonObject: JsonObject = stateResponse.getAsJsonObject("attributes")
    val tempStateAsJsonObject = JsonObject()

    tempStateAsJsonObject.add("value", stateResponse["state"])
    tempStateAsJsonObject.add("last_updated", stateResponse["last_updated"])
    tempStateAsJsonObject.add("last_changed", stateResponse["last_changed"])
    tempStateAsJsonObject.add("user_id", stateResponse["context"].asJsonObject["user_id"])
    for (attribute: String in attributesAsJsonObject.keySet()) {
        tempStateAsJsonObject.add(attribute, attributesAsJsonObject[attribute])
    }

    return tempStateAsJsonObject
}
