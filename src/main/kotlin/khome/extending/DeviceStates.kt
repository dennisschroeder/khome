package khome.extending

import khome.core.State
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class SwitchableState(override val value: SwitchableValue) : State<SwitchableValue>

data class BooleanState(override val value: Boolean) : State<Boolean>

data class InputTextState(override val value: String) : State<String>

data class InputNumberState(override val value: Float) : State<Float>

data class InputDateState(override val value: LocalDate) : State<LocalDate>

data class InputTimeState(override val value: LocalTime) : State<LocalTime>

data class InputDateTimeState(override val value: LocalDateTime) : State<LocalDateTime>
