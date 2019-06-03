package khome.core

data class Configuration(
    var host: String = "localhost",
    var port: Int = 8123,
    var accessToken: String = "<create one in home-assistant>",
    var startStateStream: Boolean = true
)