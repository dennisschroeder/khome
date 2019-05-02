package khome.core

interface MessageInterface {
    val type: String
    fun toJson(): String = serializer.toJson(this)
}