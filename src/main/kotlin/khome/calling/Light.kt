package khome.calling

import khome.core.entities.light.AbstractLightEntity

abstract class TurnOnLight(entity: AbstractLightEntity) :
    ServiceCall(Domain.LIGHT, LightService.TURN_ON) {
    override val serviceData: LightData = LightData(entity.id)
    fun serviceData(builder: LightData.() -> Unit) = serviceData.apply(builder)
}

abstract class TurnOffLight(entity: AbstractLightEntity) :
    ServiceCall(Domain.LIGHT, LightService.TURN_OFF) {
    override val serviceData: LightData = LightData(entity.id)
}

abstract class ToggleLight(entity: AbstractLightEntity) :
    ServiceCall(Domain.LIGHT, LightService.TOGGLE)

/**
 * The light context data class
 */
data class LightData(
    private val entityId: String?,
    var transition: Int? = null,
    var rgbColor: Array<Int>? = null,
    var colorName: String? = null,
    var hsColor: Array<Int>? = null,
    var xyColor: Array<Int>? = null,
    var colorTemp: Int? = null,
    var kelvin: Int? = null,
    var whiteValue: Int? = null,
    var brightness: Int? = null,
    var brightnessPct: Double? = null,
    var profile: String? = null,
    var flash: String? = null,
    var effect: String? = null

) : ServiceDataInterface

enum class LightService : ServiceInterface {
    TURN_ON, TURN_OFF, TOGGLE
}
