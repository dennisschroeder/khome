package khome.calling

class TurnOnLight :
    EntityBasedServiceCall(Domain.LIGHT, LightService.TURN_ON) {
    override val serviceData: LightData = LightData()
    fun configure(builder: LightData.() -> Unit) = serviceData.apply(builder)
}

class TurnOffLight : EntityIdOnlyServiceCall(Domain.LIGHT, LightService.TURN_OFF)

class ToggleLight :
    EntityBasedServiceCall(Domain.LIGHT, LightService.TOGGLE) {
    override val serviceData: LightData = LightData()
    fun configure(builder: LightData.() -> Unit) = serviceData.apply(builder)
}

/**
 * The light context data class
 */
class LightData(
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

) : EntityBasedServiceData()

enum class LightService : ServiceInterface {
    TURN_ON, TURN_OFF, TOGGLE
}
