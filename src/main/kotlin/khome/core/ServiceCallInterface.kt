package khome.core

interface ServiceCallInterface {
    var id: Int
    fun toJson(): String = serializer.toJson(this)
}
