package khome.core

data class DefaultConfiguration(
    override var name: String,
    override var host: String,
    override var port: Int,
    override var accessToken: String,
    override var secure: Boolean
) : Configuration
