package khome.calling

import khome.listening.getEntityInstance
import khome.core.entities.EntityInterface
import com.google.gson.annotations.SerializedName

inline fun <reified Entity : EntityInterface> ServiceCaller.openCover() {
    val entity = getEntityInstance<Entity>()
    openCover(entity.id)
}

fun ServiceCaller.openCover(entityId: String) {
    cover {
        this.entityId = entityId
        service = openCover
    }
}

inline fun <reified Entity : EntityInterface> ServiceCaller.closeCover() {
    val entity = getEntityInstance<Entity>()
    closeCover(entity.id)
}

fun ServiceCaller.closeCover(entityId: String) {
    cover {
        this.entityId = entityId
        service = closeCover
    }
}

inline fun <reified Entity : EntityInterface> ServiceCaller.setCoverPositionTo(position: Int) {
    val entity = getEntityInstance<Entity>()
    setCoverPositionTo(position, entity.id)
}

fun ServiceCaller.setCoverPositionTo(position: Int, entityId: String) {
    cover {
        this.entityId = entityId
        service = setCoverPosition
        this.position = position
    }
}

fun ServiceCaller.cover(init: CoverData.() -> Unit) {
    domain = "cover"
    serviceData = CoverData(
        entityId = "cover",
        position = null,
        setCoverPosition = "set_cover_position",
        openCover = "open_cover",
        closeCover = "close_cover"
    ).apply(init)
}

data class CoverData(
    var entityId: String,
    var position: Int?,
    @Transient val setCoverPosition: String,
    @Transient val openCover: String,
    @Transient val closeCover: String

) : ServiceDataInterface

fun ServiceCaller.covers(init: CoversData.() -> Unit) {
    domain = "cover"
    serviceData = CoversData(
        entityIds = listOf("cover"),
        position = null,
        setCoverPosition = "set_cover_position",
        openCover = "open_cover",
        closeCover = "close_cover"
    ).apply(init)
}

data class CoversData(
    @SerializedName("entity_id") var entityIds: List<String>,
    var position: Int?,
    @Transient val setCoverPosition: String,
    @Transient val openCover: String,
    @Transient val closeCover: String

) : ServiceDataInterface