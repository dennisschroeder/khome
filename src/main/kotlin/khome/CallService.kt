package khome

import com.google.gson.annotations.SerializedName
import khome.Khome.Companion.fetchNextId
import khome.Khome.Companion.incrementIdCounter
import khome.Khome.Companion.logger
import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun WebSocketSession.callService(init: CallService.() -> Unit) {
    runBlocking {
        incrementIdCounter()
        val callService = CallService(
            fetchNextId(),
            "call_service",
            null,
            null,
            null
        ).apply(init)

        launch {
            callWebSocketApi(callService.toJson())
            logger.info { "Called  Service with: " + callService.toJson() }
        }
    }
}

fun CallService.light(init: LightData.() -> Unit) {
    domain = "light"
    serviceData = LightData(
        "light",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    ).apply(init)
}

data class LightData(
    var entityId: String,
    var transition: Int?,
    var rgbColor: Array<Int>?,
    var colorName: String?,
    var hsColor: Array<Int>?,
    var xyColor: Array<Int>?,
    var colorTemp: Int?,
    var kelvin: Int?,
    var whiteValue: Int?,
    var brightness: Int?,
    var brightnessPct: Int?,
    var profile: String?

) : ServiceData

fun CallService.cover(init: CoverData.() -> Unit) {
    domain = "cover"
    serviceData = CoverData(
        entityId = "cover",
        position = null
    ).apply(init)

}

data class CoverData(
    var entityId: String,
    var position: Int?

) : ServiceData

fun CallService.covers(init: CoversData.() -> Unit) {
    domain = "cover"
    serviceData = CoversData(
        entityIds = listOf("cover"),
        position = null
    ).apply(init)

}

data class CoversData(
    @SerializedName("entity_id") var entityIds: List<String>,
    var position: Int?

) : ServiceData

fun CallService.entityId(entityId: String) {
    serviceData = EntityId(entityId)
}

data class EntityId(var entityId: String) : ServiceData