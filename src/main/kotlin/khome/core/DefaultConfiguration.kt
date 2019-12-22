package khome.core

import org.koin.core.logger.Level

data class DefaultConfiguration(
    override var host: String,
    override var port: Int,
    override var accessToken: String,
    override var secure: Boolean,
    override var startStateStream: Boolean,
    override var logLevel: String,
    override var logTime: Boolean,
    override var logTimeFormat: String,
    override var logOutput: String
) : ConfigurationInterface
