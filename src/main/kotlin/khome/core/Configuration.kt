package khome.core

interface ConfigurationInterface {
    var name: String
    var host: String
    var port: Int
    var accessToken: String
    var secure: Boolean
    var enableDefaultErrorResponseHandler: Boolean
    var enableDefaultStateChangeListenerExceptionHandler: Boolean
    var enableHassEventListenerExceptionHandler: Boolean
    var enableErrorResponseListenerExceptionHandler: Boolean
}
