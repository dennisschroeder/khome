package khome.core

import com.google.gson.FieldNamingPolicy

import com.google.gson.GsonBuilder
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlin.reflect.KClass

val serializer = GsonBuilder()
    .setPrettyPrinting()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    .create()!!

inline fun <reified M : Any> Frame.Text.toObject(): M = toObject(M::class)

fun <M : Any> Frame.Text.toObject(type: KClass<M>): M {
    val gson = serializer
    return gson.fromJson<M>(readText(), type.java)
}
