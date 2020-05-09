package khome.calling

import khome.core.entities.EntityId

class OpenCover : EntityIdOnlyServiceCall(Domain.COVER, CoverService.OPEN_COVER)

class CloseCover : EntityIdOnlyServiceCall(Domain.COVER, CoverService.CLOSE_COVER)

class ToggleCover : EntityIdOnlyServiceCall(Domain.COVER, CoverService.TOGGLE_COVER)

class SetCoverPosition : EntityBasedServiceCall(Domain.COVER, CoverService.SET_COVER_POSITION) {
    override val serviceData: CoverData = CoverData(entityId = null, position = null)
    fun configure(builder: CoverData.() -> Unit) = serviceData.apply(builder)
}

data class CoverData(
    override var entityId: EntityId?,
    var position: Int?

) : EntityBasedServiceDataInterface

enum class CoverService : ServiceInterface {
    OPEN_COVER, CLOSE_COVER, SET_COVER_POSITION, TOGGLE_COVER
}
