package khome.core

interface ConfigurationInterface {
    var host: String
    var port: Int
    var accessToken: String
    var secure: Boolean
    var startStateStream: Boolean
}
