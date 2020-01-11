package khome.scheduling

import khome.KhomeSession
import khome.core.entities.inputDateTime.AbstractTimeEntity
import khome.core.entities.system.Time
import khome.listening.LifeCycleHandler
import khome.listening.onStateChange
import org.koin.core.get
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
