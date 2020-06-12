package khome.extending

import khome.communicating.DesiredServiceData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class SettableStateValueServiceData<S>(private val value: S) : DesiredServiceData()

data class InputDateServiceData(private val date: LocalDate) : DesiredServiceData()

data class InputDateTimeServiceData(private val datetime: LocalDateTime) : DesiredServiceData()

data class InputTimeServiceData(private val time: LocalTime) : DesiredServiceData()

data class DimmableLightServiceData(private val brightness: Int) : DesiredServiceData()

data class InputSelectServiceData(val option: String) : DesiredServiceData()

data class PositionableCoverServiceData(val position: Int) : DesiredServiceData()
