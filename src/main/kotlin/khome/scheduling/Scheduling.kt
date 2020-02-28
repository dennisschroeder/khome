package khome.scheduling

import io.ktor.util.KtorExperimentalAPI
import khome.core.KhomeComponent
import khome.core.entities.inputDateTime.AbstractDateTimeEntity
import khome.core.entities.inputDateTime.AbstractTimeEntity
import khome.core.entities.system.DateTime
import khome.core.entities.system.Time
import khome.listening.LifeCycleHandler
import khome.listening.onStateChange
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.get
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
inline fun <reified TimeEntity : AbstractTimeEntity> KhomeComponent.onTime(noinline callback: LocalTime.() -> Unit): LifeCycleHandler {
    return onStateChange<Time> {
        val executeAt: LocalTime = get<TimeEntity>().time
        if (executeAt == currentLocalTime) {
            callback(executeAt)
            logger.debug { "Executed scheduled task at: $currentLocalTime" }
        }
    }
}

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
fun KhomeComponent.onTime(executeAt: String, callback: LocalTime.() -> Unit): LifeCycleHandler {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:m")
    val localTime: LocalTime = LocalTime.parse(executeAt, formatter)
    return onTime(localTime, callback)
}

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
fun KhomeComponent.onTime(executeAt: LocalTime, callback: LocalTime.() -> Unit) =
    onStateChange<Time> {
        if (executeAt == currentLocalTime) callback(executeAt)
    }

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
inline fun <reified DateTimeEntity : AbstractDateTimeEntity> KhomeComponent.onDateTime(noinline callback: LocalDateTime.() -> Unit): LifeCycleHandler =
    onStateChange<DateTime> {
        val executeAt: LocalDateTime = get<DateTimeEntity>().dateTime
        if (executeAt == currentLocalDateTime) {
            callback(executeAt)
            logger.debug { "Executed scheduled task at: $currentLocalDateTime" }
        }
    }


@KtorExperimentalAPI
@ObsoleteCoroutinesApi
fun KhomeComponent.onDateTime(
    executeAtDate: String,
    executeAtTime: String,
    callback: LocalDateTime.() -> Unit
): LifeCycleHandler {
    val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val localDateTime: LocalDateTime = LocalDateTime.parse("${executeAtDate}T$executeAtTime", formatter)
    return onDateTime(localDateTime, callback)
}

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
fun KhomeComponent.onDateTime(executeAt: LocalDateTime, callback: LocalDateTime.() -> Unit) =
    onStateChange<DateTime> {
        if (executeAt == currentLocalDateTime) {
            callback(executeAt)
            logger.debug { "Executed scheduled task at: $currentLocalDateTime" }
        }
    }
