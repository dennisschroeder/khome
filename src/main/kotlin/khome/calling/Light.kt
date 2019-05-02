package khome.calling


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

) : ServiceDataInterface