package khome.scheduling

import io.ktor.util.KtorExperimentalAPI
import khome.KhomeComponent
import khome.core.entities.system.DateTime
import khome.core.entities.system.Time
import khome.observing.onStateChange
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.get
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@ExperimentalStdlibApi
@ObsoleteCoroutinesApi
@KtorExperimentalAPI
inline fun <reified TimeEntity : khome.core.entities.inputDateTime.TimeEntity> KhomeComponent.onTimeDaily(noinline callback: LocalTime.() -> Unit): Time {
    return onStateChange { _, newState ->
        val executeAt: LocalTime = get<TimeEntity>().time
        val currentLocalTime = LocalTime.parse(newState.state as? CharSequence, DateTimeFormatter.ofPattern("H:m"))
        if (executeAt == currentLocalTime) {
            callback(executeAt)
            logger.debug { "Executed scheduled task at: $currentLocalTime" }
        }
    }
}

@ExperimentalStdlibApi
@KtorExperimentalAPI
@ObsoleteCoroutinesApi
fun KhomeComponent.onTimeDaily(executeAt: String, callback: LocalTime.() -> Unit): Time {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:m")
    val localTime: LocalTime = LocalTime.parse(executeAt, formatter)
    return onTimeDaily(localTime, callback)
}

@ExperimentalStdlibApi
@KtorExperimentalAPI
@ObsoleteCoroutinesApi
fun KhomeComponent.onTimeDaily(executeAt: LocalTime, callback: LocalTime.() -> Unit): Time {
    val executeAtWithoutNanos = executeAt.let { LocalTime.of(it.hour, it.minute) }
    return onStateChange<Time> { _, newState ->
        val currentLocalTime = LocalTime.parse(newState.state as? CharSequence, DateTimeFormatter.ofPattern("H:m"))
        if (executeAtWithoutNanos == currentLocalTime) callback(executeAt)
    }
}

@ExperimentalStdlibApi
@KtorExperimentalAPI
@ObsoleteCoroutinesApi
inline fun <reified DateTimeEntity : khome.core.entities.inputDateTime.DateTimeEntity> KhomeComponent.onDateTime(
    noinline callback: LocalDateTime.() -> Unit
): DateTime =
    onStateChange { _, newState ->
        val executeAt: LocalDateTime = get<DateTimeEntity>().dateTime
        val currentLocalDateTime =
            LocalDateTime.parse(newState.state as? CharSequence, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        if (executeAt == currentLocalDateTime) {
            callback(executeAt)
            logger.debug { "Executed scheduled task at: $currentLocalDateTime" }
        }
    }

@ExperimentalStdlibApi
@KtorExperimentalAPI
@ObsoleteCoroutinesApi
fun KhomeComponent.onDateTime(
    executeAtDate: String,
    executeAtTime: String,
    callback: LocalDateTime.() -> Unit
): DateTime {
    val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val localDateTime: LocalDateTime = LocalDateTime.parse("${executeAtDate}T$executeAtTime", formatter)
    return onDateTime(localDateTime, callback)
}

@ExperimentalStdlibApi
@KtorExperimentalAPI
@ObsoleteCoroutinesApi
fun KhomeComponent.onDateTime(executeAt: LocalDateTime, callback: LocalDateTime.() -> Unit): DateTime {
    val executeAtWithoutNanos = executeAt.let {
        LocalDateTime.of(
            it.year,
            it.month,
            it.dayOfMonth,
            it.hour,
            it.minute
        )
    }
    return onStateChange<DateTime> { _, newState ->
        val currentLocalDateTime =
            LocalDateTime.parse(newState.state as? CharSequence, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        if (executeAtWithoutNanos == currentLocalDateTime) {
            callback(executeAt)
            logger.debug { "Executed scheduled task at: $currentLocalDateTime" }
        }
    }
}
