package khome.core

interface Configuration {
    var name: String
    var host: String
    var port: Int
    var accessToken: String
    var secure: Boolean
}
