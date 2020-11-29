package khome.core.mapping

import com.google.gson.Gson
import com.google.gson.JsonElement
import io.ktor.util.KtorExperimentalAPI
import khome.core.koin.KhomeComponent
import kotlinx.coroutines.ObsoleteCoroutinesApi

interface ObjectMapperInterface {
    fun <Target> fromJson(json: String, type: Class<Target>): Target
    fun <Target> fromJson(json: JsonElement, type: Class<Target>): Target
    fun <Destination> toJson(from: Destination): String
}

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
class ObjectMapper(private val delegate: Gson) : KhomeComponent, ObjectMapperInterface {
    override fun <Target> fromJson(json: JsonElement, type: Class<Target>): Target = delegate.fromJson(json, type)
    override fun <Target> fromJson(json: String, type: Class<Target>): Target = delegate.fromJson(json, type)
    override fun <Destination> toJson(from: Destination): String = delegate.toJson(from)
}

inline fun <reified Target> ObjectMapperInterface.fromJson(json: String): Target = fromJson(json, Target::class.java)
inline fun <reified Target> ObjectMapperInterface.fromJson(json: JsonElement): Target = fromJson(json, Target::class.java)
