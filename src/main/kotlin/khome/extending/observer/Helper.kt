package khome.extending.observer

import khome.KhomeApplication
import khome.entities.State
import khome.extending.InputBooleanAttributes
import khome.extending.InputBooleanSnapshot
import khome.extending.InputDateAttributes
import khome.extending.InputDateSnapshot
import khome.extending.InputDateState
import khome.extending.InputDateTimeAttributes
import khome.extending.InputDateTimeSnapshot
import khome.extending.InputDateTimeState
import khome.extending.InputNumberAttributes
import khome.extending.InputNumberSnapshot
import khome.extending.InputNumberState
import khome.extending.InputSelectAttributes
import khome.extending.InputSelectSnapshot
import khome.extending.InputTextAttributes
import khome.extending.InputTextSnapshot
import khome.extending.InputTextState
import khome.extending.InputTimeAttributes
import khome.extending.InputTimeSnapshot
import khome.extending.InputTimeState
import khome.extending.SwitchableState
import khome.observability.SwitchableObserver
import kotlinx.coroutines.CoroutineScope

@Suppress("FunctionName")
fun KhomeApplication.InputTextObserver(f: (InputTextSnapshot, SwitchableObserver<InputTextState, InputTextAttributes>) -> Unit): SwitchableObserver<InputTextState, InputTextAttributes> =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.InputTextAsyncObserver(f: suspend CoroutineScope.(InputTextSnapshot, SwitchableObserver<InputTextState, InputTextAttributes>) -> Unit): SwitchableObserver<InputTextState, InputTextAttributes> =
    AsyncObserver(f)

@Suppress("FunctionName")
fun KhomeApplication.InputNumberObserver(f: (InputNumberSnapshot, SwitchableObserver<InputNumberState, InputNumberAttributes>) -> Unit): SwitchableObserver<InputNumberState, InputNumberAttributes> =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.InputNumberAsyncObserver(f: suspend CoroutineScope.(InputNumberSnapshot, SwitchableObserver<InputNumberState, InputNumberAttributes>) -> Unit): SwitchableObserver<InputNumberState, InputNumberAttributes> =
    AsyncObserver(f)

@Suppress("FunctionName")
fun <SV> KhomeApplication.InputSelectObserver(f: (InputSelectSnapshot<SV>, SwitchableObserver<State<SV>, InputSelectAttributes>) -> Unit): SwitchableObserver<State<SV>, InputSelectAttributes> =
    Observer(f)

@Suppress("FunctionName")
fun <SV> KhomeApplication.InputSelectAsyncObserver(f: suspend CoroutineScope.(InputSelectSnapshot<SV>, SwitchableObserver<State<SV>, InputSelectAttributes>) -> Unit): SwitchableObserver<State<SV>, InputSelectAttributes> =
    AsyncObserver(f)

@Suppress("FunctionName")
fun KhomeApplication.InputBooleanObserver(f: (InputBooleanSnapshot, SwitchableObserver<SwitchableState, InputBooleanAttributes>) -> Unit): SwitchableObserver<SwitchableState, InputBooleanAttributes> =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.InputBooleanAsyncObserver(f: suspend CoroutineScope.(InputBooleanSnapshot, SwitchableObserver<SwitchableState, InputBooleanAttributes>) -> Unit): SwitchableObserver<SwitchableState, InputBooleanAttributes> =
    AsyncObserver(f)

@Suppress("FunctionName")
fun KhomeApplication.InputTimeObserver(f: (InputTimeSnapshot, SwitchableObserver<InputTimeState, InputTimeAttributes>) -> Unit): SwitchableObserver<InputTimeState, InputTimeAttributes> =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.InputTimeAsyncObserver(f: suspend CoroutineScope.(InputTimeSnapshot, SwitchableObserver<InputTimeState, InputTimeAttributes>) -> Unit): SwitchableObserver<InputTimeState, InputTimeAttributes> =
    AsyncObserver(f)

@Suppress("FunctionName")
fun KhomeApplication.InputDateObserver(f: (InputDateSnapshot, SwitchableObserver<InputDateState, InputDateAttributes>) -> Unit): SwitchableObserver<InputDateState, InputDateAttributes> =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.InputDateAsyncObserver(f: suspend CoroutineScope.(InputDateSnapshot, SwitchableObserver<InputDateState, InputDateAttributes>) -> Unit): SwitchableObserver<InputDateState, InputDateAttributes> =
    AsyncObserver(f)

@Suppress("FunctionName")
fun KhomeApplication.InputDateTimeObserver(f: (InputDateTimeSnapshot, SwitchableObserver<InputDateTimeState, InputDateTimeAttributes>) -> Unit): SwitchableObserver<InputDateTimeState, InputDateTimeAttributes> =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.InputDateTimeAsyncObserver(f: suspend CoroutineScope.(InputDateTimeSnapshot, SwitchableObserver<InputDateTimeState, InputDateTimeAttributes>) -> Unit): SwitchableObserver<InputDateTimeState, InputDateTimeAttributes> =
    AsyncObserver(f)
