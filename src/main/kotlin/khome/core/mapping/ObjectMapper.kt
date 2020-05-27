package khome.core.mapping

import com.google.gson.Gson
import com.google.gson.JsonElement
import io.ktor.util.KtorExperimentalAPI
import khome.core.koin.KhomeComponent
import kotlinx.coroutines.ObsoleteCoroutinesApi

interface ObjectMapperInterface

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
class ObjectMapper(val delegate: Gson) : KhomeComponent, ObjectMapperInterface {
    inline fun <reified Target> fromJson(json: String): Target = delegate.fromJson<Target>(json, Target::class.java)
    fun <Target> fromJson(json: JsonElement, type: Class<Target>): Target = delegate.fromJson<Target>(json, type)
    fun <Target> fromJson(json: String, type: Class<Target>): Target = delegate.fromJson<Target>(json, type)
    fun <Destination> toJson(from: Destination): String = delegate.toJson(from)
}
