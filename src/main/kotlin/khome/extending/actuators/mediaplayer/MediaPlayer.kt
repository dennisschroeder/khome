package khome.extending.actuators.mediaplayer

import khome.KhomeApplication
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.EntityId
import khome.entities.State
import khome.extending.Actuator

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> KhomeApplication.MediaPlayer(
    objectId: String,
    serviceCommandResolver: ServiceCommandResolver<S>
) = Actuator<S, A>(EntityId(domain = "media_player", objectId = objectId), serviceCommandResolver)
