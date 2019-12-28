package khome.calling

import com.google.gson.annotations.SerializedName
import khome.core.entities.cover.AbstractCoverEntity

fun ServiceCall.openCover(entity: AbstractCoverEntity) =
    cover {
        entityId = entity.id
        service = CoverServices.OPEN_COVER
    }

fun ServiceCall.closeCover(entity: AbstractCoverEntity) =
    cover {
        entityId = entity.id
        service = CoverServices.CLOSE_COVER
    }

fun ServiceCall.setCoverPositionTo(entity: AbstractCoverEntity, position: Int) =
    cover {
        entityId = entity.id
        service = CoverServices.SET_COVER_POSITION
        this.position = position
    }

fun ServiceCall.cover(init: CoverData.() -> Unit) {
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

fun ServiceCall.covers(init: CoversData.() -> Unit) {
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

enum class CoverServices : ServiceInterface {
    OPEN_COVER, CLOSE_COVER, SET_COVER_POSITION
}
