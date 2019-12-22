package khome.core

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
