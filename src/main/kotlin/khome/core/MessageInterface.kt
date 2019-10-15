package khome.core

interface MessageInterface {
    fun toJson(): String = serializer.toJson(this)
}
