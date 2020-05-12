package khome.scheduling

import io.ktor.util.KtorExperimentalAPI
import khome.core.entities.inputDateTime.DateTimeEntity
import khome.core.entities.system.DateTime
import khome.core.entities.system.Time
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.get
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
inline fun <reified TimeEntity : khome.core.entities.inputDateTime.TimeEntity> Time.onTimeDaily(noinline callback: LocalTime.() -> Unit) {
    return onStateChange { _, newState ->
        val executeAt: LocalTime = get<TimeEntity>().time
        val currentLocalTime = LocalTime.parse(newState.value as? CharSequence, DateTimeFormatter.ofPattern("H:m"))
        if (executeAt == currentLocalTime) {
            callback(executeAt)
            logger.debug { "Executed scheduled task at: $currentLocalTime" }
        }
    }
}

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
fun Time.onTimeDaily(executeAt: String, callback: LocalTime.() -> Unit) {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:m")
    val localTime: LocalTime = LocalTime.parse(executeAt, formatter)
    return onTimeDaily(localTime, callback)
}

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
fun Time.onTimeDaily(executeAt: LocalTime, callback: LocalTime.() -> Unit) {
    val executeAtWithoutNanos = executeAt.let { LocalTime.of(it.hour, it.minute) }
    return onStateChange { _, newState ->
        val currentLocalTime = LocalTime.parse(newState.value as? CharSequence, DateTimeFormatter.ofPattern("H:m"))
        if (executeAtWithoutNanos == currentLocalTime) callback(executeAt)
    }
}

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
inline fun <reified DateTimeEntityType : DateTimeEntity> DateTime.onDateTime(
    noinline callback: LocalDateTime.() -> Unit
) =
    onStateChange { _, newState ->
        val executeAt: LocalDateTime = get<DateTimeEntity>().dateTime
        val currentLocalDateTime =
            LocalDateTime.parse(newState.value as? CharSequence, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        if (executeAt == currentLocalDateTime) {
            callback(executeAt)
            logger.debug { "Executed scheduled task at: $currentLocalDateTime" }
        }
    }

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
fun DateTime.onDateTime(
    executeAtDate: String,
    executeAtTime: String,
    callback: LocalDateTime.() -> Unit
) {
    val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val localDateTime: LocalDateTime = LocalDateTime.parse("${executeAtDate}T$executeAtTime", formatter)
    return onDateTime(localDateTime, callback)
}

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
fun DateTime.onDateTime(executeAt: LocalDateTime, callback: LocalDateTime.() -> Unit) {
    val executeAtWithoutNanos = executeAt.let {
        LocalDateTime.of(
            it.year,
            it.month,
            it.dayOfMonth,
            it.hour,
            it.minute
        )
    }
    onStateChange { _, newState ->
        val currentLocalDateTime =
            LocalDateTime.parse(newState.value as? CharSequence, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        if (executeAtWithoutNanos == currentLocalDateTime) {
            callback(executeAt)
            logger.debug { "Executed scheduled task at: $currentLocalDateTime" }
        }
    }
}
