package khome.helper

import khome.KhomeApplication
import khome.entities.EntityId

typealias AttributesMap = Map<String, Any>

inline fun <reified Attributes> KhomeApplication.createInputNumber(objectId: String) =
    createActuator<Double, Attributes>(EntityId("input_number", objectId))

inline fun <reified Attributes> KhomeApplication.createInputBoolean(objectId: String) =
    createActuator<SwitchableValue, Attributes>(EntityId("input_boolean", objectId))

inline fun <reified State, reified Attributes> KhomeApplication.createMediaPlayer(objectId: String) =
    createActuator<State, Attributes>(EntityId("media_player", objectId))

inline fun <reified Attributes> KhomeApplication.createCover(objectId: String) =
    createActuator<SwitchableValue, Attributes>(EntityId("cover", objectId))

inline fun <reified Attributes> KhomeApplication.createLight(objectId: String) =
    createActuator<SwitchableValue, Attributes>(EntityId("light", objectId))

/**
 * Base helper
 */
inline fun <reified S, reified SA> KhomeApplication.createSensor(id: EntityId) =
    createSensor<S, SA>(id, S::class, SA::class)

inline fun <reified S, reified SA> KhomeApplication.createActuator(id: EntityId) =
    createActuator<S, SA>(id, S::class, SA::class)
