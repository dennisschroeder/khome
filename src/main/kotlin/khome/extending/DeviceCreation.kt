package khome.extending

import khome.KhomeApplication
import khome.entities.EntityId
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun KhomeApplication.createInputText(objectId: String) =
    createActuator<String, InputTextAttributes>(EntityId("input_text", objectId))

fun KhomeApplication.createInputNumber(objectId: String) =
    createActuator<Float, InputNumberAttributes>(EntityId("input_number", objectId))

fun KhomeApplication.createInputBoolean(objectId: String) =
    createActuator<SwitchableValue, InputBooleanAttributes>(EntityId("input_boolean", objectId))

inline fun <reified State : Enum<State>> KhomeApplication.createInputSelect(objectId: String) =
    createActuator<State, InputSelectAttributes>(EntityId("input_select", objectId))

fun KhomeApplication.createInputDate(objectId: String) =
    createActuator<LocalDate, InputDateAttributes>(EntityId("input_datetime", objectId))

fun KhomeApplication.createInputTime(objectId: String) =
    createActuator<LocalTime, InputTimeAttributes>(EntityId("input_datetime", objectId))

fun KhomeApplication.createInputDateTime(objectId: String) =
    createActuator<LocalDateTime, InputDateTimeAttributes>(EntityId("input_datetime", objectId))

inline fun <reified State, reified Attributes> KhomeApplication.createMediaPlayer(objectId: String) =
    createActuator<State, Attributes>(EntityId("media_player", objectId))

inline fun <reified State : Enum<State>, reified Attributes> KhomeApplication.createCover(objectId: String) =
    createActuator<State, Attributes>(EntityId("cover", objectId))

inline fun <reified Attributes> KhomeApplication.createLight(objectId: String) =
    createActuator<SwitchableValue, Attributes>(EntityId("light", objectId))

/**
 * Base helper
 */
inline fun <reified S, reified SA> KhomeApplication.createSensor(id: EntityId) =
    createSensor<S, SA>(id, S::class, SA::class)

inline fun <reified S, reified SA> KhomeApplication.createActuator(id: EntityId) =
    createActuator<S, SA>(id, S::class, SA::class)
