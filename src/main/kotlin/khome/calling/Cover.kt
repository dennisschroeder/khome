package khome.calling

import khome.core.entities.EntityInterface
import com.google.gson.annotations.SerializedName

fun ServiceCaller.openCover(entityId: String) =
    cover {
        this.entityId = entityId
        service = CoverServices.OPEN_COVER
    }

fun ServiceCaller.closeCover(entity: EntityInterface) =
    cover {
        entityId = entity.id
        service = CoverServices.CLOSE_COVER
    }

fun ServiceCaller.setCoverPositionTo(position: Int, entity: EntityInterface) =
    cover {
        entityId = entity.id
        service = CoverServices.SET_COVER_POSITION
        this.position = position
    }

fun ServiceCaller.cover(init: CoverData.() -> Unit) {
    domain = Domain.COVER
    serviceData = CoverData(
        entityId = null,
        position = null
    ).apply(init)
}

data class CoverData(
    override var entityId: String?,
    var position: Int?

) : ServiceDataInterface

fun ServiceCaller.covers(init: CoversData.() -> Unit) {
    domain = Domain.COVER
    serviceData = CoversData(
        entityId = null,
        entityIds = listOf("cover"),
        position = null
    ).apply(init)
}

data class CoversData(
    @Transient override var entityId: String?,
    @SerializedName("entity_id") var entityIds: List<String>,
    var position: Int?
) : ServiceDataInterface

enum class CoverServices : ServiceInterface{
    OPEN_COVER, CLOSE_COVER, SET_COVER_POSITION
}