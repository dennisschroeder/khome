package khome.core

data class DefaultConfiguration(
    override var host: String,
    override var port: Int,
    override var accessToken: String,
    override var secure: Boolean
) : ConfigurationInterface
