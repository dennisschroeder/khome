package khome.calling

import com.google.gson.annotations.SerializedName

fun ServiceCaller.mediaPlayer(init: MediaData.() -> Unit) {
    domain = "media_player"
    serviceData = MediaData(
        null,
        null,
        null,
        null
    ).apply(init)
}

data class MediaData(
    override var entityId: String?,
    var isVolumeMuted: Boolean?,
    var mediaContentId: String?,
    var mediaContentType: MediaContentType?
) : ServiceDataInterface

enum class MediaContentType {

    @SerializedName("music") MUSIC,
    @SerializedName("tvshow") TVSHOW,
    @SerializedName("video") VIDEO,
    @SerializedName("episode") EPISODE,
    @SerializedName("channel") CHANNEL,
    @SerializedName("playlist") PLAYLIST
}