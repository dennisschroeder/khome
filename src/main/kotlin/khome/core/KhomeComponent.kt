package khome.core

import io.ktor.util.KtorExperimentalAPI
import khome.calling.ServiceDataInterface
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.mapping.ObjectMapper
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.get

@ObsoleteCoroutinesApi
@KtorExperimentalAPI

abstract class KhomeComponent() : KhomeKoinComponent() {

    fun ServiceDataInterface.toJson(): String = get<ObjectMapper>().toJson(this)
    fun MessageInterface.toJson(): String = get<ObjectMapper>().toJson(this)
}
