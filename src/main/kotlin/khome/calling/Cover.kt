package khome.calling

import com.google.gson.annotations.SerializedName

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