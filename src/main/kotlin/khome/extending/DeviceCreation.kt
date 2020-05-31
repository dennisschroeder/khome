package khome.extending

import khome.KhomeApplication
import khome.entities.EntityId

fun KhomeApplication.createInputText(objectId: String) =
    createActuator<String, InputTextAttributes>(EntityId("input_text", objectId))

fun KhomeApplication.createInputNumber(objectId: String) =
    createActuator<Float, InputNumberAttributes>(EntityId("input_number", objectId))

fun KhomeApplication.createInputBoolean(objectId: String) =
    createActuator<SwitchableValue, InputBooleanAttributes>(EntityId("input_boolean", objectId))

inline fun<reified State : Enum<State>> KhomeApplication.createInputSelect(objectId: String) =
    createActuator<State, InputSelectAttributes>(EntityId("input_select", objectId))

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
