package khome.core

data class Configuration(
    var host: String = "localhost",
    var port: Int = 8123,
    var accessToken: String = "<create one in home-assistant>",
    var secure: Boolean = false,
    var startStateStream: Boolean = true,
    var logLevel: String = "INFO",
    var logTime: Boolean = true,
    var logTimeFormat: String = "yyyy-MM-dd HH:mm:ss",
    var logOutput: String = "System.out",
    var reConnectionPeriod: Long = 30000
)
