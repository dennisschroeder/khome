package khome.core

abstract class Configuration(
    override var host: String = "localhost",
    override var port: Int = 8123,
    override var accessToken: String = "<create one in home-assistant>",
    override var secure: Boolean = false,
    override var startStateStream: Boolean = true,
    override var logLevel: String = "INFO",
    override var logTime: Boolean = true,
    override var logTimeFormat: String = "yyyy-MM-dd HH:mm:ss",
    override var logOutput: String = "System.out"
) : ConfigurationInterface

interface ConfigurationInterface {
    var host: String
    var port: Int
    var accessToken: String
    var secure: Boolean
    var startStateStream: Boolean
    var logLevel: String
    var logTime: Boolean
    var logTimeFormat: String
    var logOutput: String
}
