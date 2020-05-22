package khome.calling

class OpenCover : EntityIdOnlyServiceCall(HassDomain.COVER, CoverService.OPEN_COVER)

class CloseCover : EntityIdOnlyServiceCall(HassDomain.COVER, CoverService.CLOSE_COVER)

class ToggleCover : EntityIdOnlyServiceCall(HassDomain.COVER, CoverService.TOGGLE_COVER)

class SetCoverPosition : EntityBasedServiceCall(HassDomain.COVER, CoverService.SET_COVER_POSITION) {
    override val serviceData: CoverData = CoverData(entityId = null, position = null)
    fun configure(builder: CoverData.() -> Unit) = serviceData.apply(builder)
}

data class CoverData(
    override var entityId: String?,
    var position: Int?

) : EntityBasedServiceDataInterface

enum class CoverService : ServiceInterface {
    OPEN_COVER, CLOSE_COVER, SET_COVER_POSITION, TOGGLE_COVER
}
