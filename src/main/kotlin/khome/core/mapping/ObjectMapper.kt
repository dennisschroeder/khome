package khome.core.mapping

import com.google.gson.Gson
import io.ktor.util.KtorExperimentalAPI
import khome.core.koin.KhomeKoinComponent
import kotlinx.coroutines.ObsoleteCoroutinesApi

interface ObjectMapperInterface

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
class ObjectMapper(val delegate: Gson) : KhomeKoinComponent, ObjectMapperInterface {
    inline fun <reified Target> fromJson(json: String): Target = delegate.fromJson<Target>(json, Target::class.java)
    fun <Destination> toJson(from: Destination): String = delegate.toJson(from)
}
