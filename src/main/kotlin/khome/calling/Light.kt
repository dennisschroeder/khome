package khome.calling

/**
 * The service call builder for the domain light. Pass an lambda with receiver
 * to have access to the [LightData] class to configure the payload sent to the
 * home-assistant websocket api. See also the [home-assistant documentation](https://www.home-assistant.io/components/light/)
 *
 * @receiver [LightData]
 */
fun ServiceCall.light(init: LightData.() -> Unit) {
    domain = Domain.LIGHT
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

/**
 * The light context data class
 */
data class LightData(
    override var entityId: String?,
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

) : ServiceDataInterface
