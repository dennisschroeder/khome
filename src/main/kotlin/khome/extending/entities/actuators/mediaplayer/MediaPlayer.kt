package khome.extending.entities.actuators.mediaplayer

import khome.KhomeApplication
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.values.EntityId
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import khome.values.ObjectId
import khome.values.domain

typealias MediaPlayer<S, A> = Actuator<S, A>

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> KhomeApplication.MediaPlayer(
    objectId: ObjectId,
    serviceCommandResolver: ServiceCommandResolver<S>
): MediaPlayer<S, A> = Actuator(EntityId.fromPair("media_player".domain to objectId), serviceCommandResolver)

enum class MediaPlayerService {
    VOLUME_MUTE, VOLUME_SET, SELECT_SOURCE, MEDIA_PLAY, MEDIA_PAUSE, SEEK_POSITION
}
