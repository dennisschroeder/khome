package khome.entities

data class EntityId(val domain: String, val objectId: String) {
    override fun toString(): String = "$domain.$objectId"

    companion object {
        fun fromString(apiName: String): EntityId {
            val parts = apiName.split(".")
            assert(parts.size == 2)
            val (domain, id) = parts
            return EntityId(domain, id)
        }
    }
}
