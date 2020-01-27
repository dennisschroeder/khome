package khome.scheduling

import khome.KhomeSession
import khome.core.entities.inputDateTime.AbstractDateTimeEntity
import khome.core.entities.inputDateTime.AbstractTimeEntity
import khome.core.entities.system.DateTime
import khome.core.entities.system.Time
import khome.core.logger
import khome.listening.LifeCycleHandler
import khome.listening.onStateChange
import org.koin.core.get
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

inline fun <reified TimeEntity : AbstractTimeEntity> KhomeSession.onTime(noinline callback: LocalTime.() -> Unit): LifeCycleHandler {
    val timeEntity: TimeEntity = get()
    val executeAt: LocalTime = timeEntity.time
    return onTime(executeAt, callback)
}

fun KhomeSession.onTime(executeAt: String, callback: LocalTime.() -> Unit): LifeCycleHandler {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:m")
    val localTime: LocalTime = LocalTime.parse(executeAt, formatter)
    return onTime(localTime, callback)
}

fun KhomeSession.onTime(executeAt: LocalTime, callback: LocalTime.() -> Unit) =
    onStateChange<Time> {
        if (executeAt == currentLocalTime) callback(executeAt)
    }

inline fun <reified TimeEntity : AbstractDateTimeEntity> KhomeSession.onDateTime(noinline callback: LocalDateTime.() -> Unit): LifeCycleHandler {
    val timeEntity: TimeEntity = get()
    val executeAt: LocalDateTime = timeEntity.dateTime
    return onDateTime(executeAt, callback)
}

fun KhomeSession.onDateTime(
    executeAtDate: String,
    executeAtTime: String,
    callback: LocalDateTime.() -> Unit
): LifeCycleHandler {
    val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val localDateTime: LocalDateTime = LocalDateTime.parse("${executeAtDate}T$executeAtTime", formatter)
    logger.info { localDateTime }
    return onDateTime(localDateTime, callback)
}

fun KhomeSession.onDateTime(executeAt: LocalDateTime, callback: LocalDateTime.() -> Unit) =
    onStateChange<DateTime> {
        if (executeAt == currentLocalDateTime) {
            callback(executeAt)
            logger.debug { "Executed scheduled task at: $currentLocalDateTime" }
        }
    }
