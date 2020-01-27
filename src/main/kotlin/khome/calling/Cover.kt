package khome.calling

import com.google.gson.annotations.SerializedName
import khome.core.entities.EntityInterface
import khome.core.entities.cover.AbstractCoverEntity

abstract class OpenCover(entity: EntityInterface) :
    ServiceCall(Domain.COVER, CoverService.OPEN_COVER) {
    override val serviceData: EntityId = EntityId(entity.id)
}

abstract class CloseCover(entity: EntityInterface) :
    ServiceCall(Domain.COVER, CoverService.CLOSE_COVER) {
    override val serviceData: EntityId = EntityId(entity.id)
}

abstract class SetCoverPosition(entity: AbstractCoverEntity) :
    ServiceCall(Domain.COVER, CoverService.SET_COVER_POSITION) {
    override val serviceData: CoverData = CoverData(entity.id)
    fun serviceData(builder: CoverData.() -> Unit) = serviceData.apply(builder)
}

abstract class ToggleCover(entity: AbstractCoverEntity) :
    ServiceCall(Domain.COVER, CoverService.TOGGLE_COVER) {
    override val serviceData: EntityId = EntityId(entity.id)
}

data class CoverData(
    private val entityId: String?,
    var position: Int? = null

) : ServiceDataInterface

data class CoversData(
    @SerializedName("entity_id") var entityIds: List<String>,
    var position: Int?
) : ServiceDataInterface

enum class CoverService : ServiceInterface {
    OPEN_COVER, CLOSE_COVER, SET_COVER_POSITION, TOGGLE_COVER
}
