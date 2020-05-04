package khome.scheduling

import io.ktor.util.KtorExperimentalAPI
import khome.KhomeComponent
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
inline fun <reified TimeEntity : AbstractTimeEntity> KhomeComponent.onTimeDaily(noinline callback: LocalTime.() -> Unit): LifeCycleHandler {
    return onStateChange<Time> { entity ->
        val executeAt: LocalTime = get<TimeEntity>().time
        if (executeAt == entity.currentLocalTime) {
            callback(executeAt)
            logger.debug { "Executed scheduled task at: ${entity.currentLocalTime}" }
        }
    }
}

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
fun KhomeComponent.onTimeDaily(executeAt: String, callback: LocalTime.() -> Unit): LifeCycleHandler {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:m")
    val localTime: LocalTime = LocalTime.parse(executeAt, formatter)
    return onTimeDaily(localTime, callback)
}

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
fun KhomeComponent.onTimeDaily(executeAt: LocalTime, callback: LocalTime.() -> Unit): LifeCycleHandler {
    val executeAtWithoutNanos = executeAt.let { LocalTime.of(it.hour, it.minute) }
    return onStateChange<Time> { entity ->
        if (executeAtWithoutNanos == entity.currentLocalTime) callback(executeAt)
    }
}

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
inline fun <reified DateTimeEntity : AbstractDateTimeEntity> KhomeComponent.onDateTime(noinline callback: LocalDateTime.() -> Unit): LifeCycleHandler =
    onStateChange<DateTime> { entity ->
        val executeAt: LocalDateTime =
            get<DateTimeEntity>().dateTime
        if (executeAt == entity.currentLocalDateTime) {
            callback(executeAt)
            logger.debug { "Executed scheduled task at: ${entity.currentLocalDateTime}" }
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
fun KhomeComponent.onDateTime(executeAt: LocalDateTime, callback: LocalDateTime.() -> Unit): LifeCycleHandler {
    val executeAtWithoutNanos = executeAt.let {
        LocalDateTime.of(
            it.year,
            it.month,
            it.dayOfMonth,
            it.hour,
            it.minute
        )
    }
    return onStateChange<DateTime> { entity ->
        if (executeAtWithoutNanos == entity.currentLocalDateTime) {
            callback(executeAt)
            logger.debug { "Executed scheduled task at: ${entity.currentLocalDateTime}" }
        }
    }
}
