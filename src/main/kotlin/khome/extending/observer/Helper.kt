package khome.extending.observer

import khome.KhomeApplication
import khome.extending.InputBooleanSnapshot
import khome.extending.InputDateSnapshot
import khome.extending.InputDateTimeSnapshot
import khome.extending.InputNumberSnapshot
import khome.extending.InputSelectSnapshot
import khome.extending.InputTextSnapshot
import khome.extending.InputTimeSnapshot
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineScope

@Suppress("FunctionName")
fun KhomeApplication.InputTextObserver(f: (InputTextSnapshot, Switchable) -> Unit): Switchable =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.InputTextAsyncObserver(f: suspend CoroutineScope.(InputTextSnapshot, Switchable) -> Unit): Switchable =
    AsyncObserver(f)

@Suppress("FunctionName")
fun KhomeApplication.InputNumberObserver(f: (InputNumberSnapshot, Switchable) -> Unit) =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.InputNumberAsyncObserver(f: suspend CoroutineScope.(InputNumberSnapshot, Switchable) -> Unit): Switchable =
    AsyncObserver(f)

@Suppress("FunctionName")
fun <SV> KhomeApplication.InputSelectObserver(f: (InputSelectSnapshot<SV>, Switchable) -> Unit): Switchable =
    Observer(f)

@Suppress("FunctionName")
fun <SV> KhomeApplication.InputSelectAsyncObserver(f: suspend CoroutineScope.(InputSelectSnapshot<SV>, Switchable) -> Unit): Switchable =
    AsyncObserver(f)

@Suppress("FunctionName")
fun KhomeApplication.InputBooleanObserver(f: (InputBooleanSnapshot, Switchable) -> Unit): Switchable =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.InputBooleanAsyncObserver(f: suspend CoroutineScope.(InputBooleanSnapshot, Switchable) -> Unit): Switchable =
    AsyncObserver(f)

@Suppress("FunctionName")
fun KhomeApplication.InputTimeObserver(f: (InputTimeSnapshot, Switchable) -> Unit): Switchable =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.InputTimeAsyncObserver(f: suspend CoroutineScope.(InputDateSnapshot, Switchable) -> Unit): Switchable =
    AsyncObserver(f)

@Suppress("FunctionName")
fun KhomeApplication.InputDateObserver(f: (InputDateSnapshot, Switchable) -> Unit): Switchable =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.InputDateAsyncObserver(f: suspend CoroutineScope.(InputDateSnapshot, Switchable) -> Unit): Switchable =
    AsyncObserver(f)

@Suppress("FunctionName")
fun KhomeApplication.InputDateTimeObserver(f: (InputDateTimeSnapshot, Switchable) -> Unit): Switchable =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.InputDateTimeAsyncObserver(f: suspend CoroutineScope.(InputDateTimeSnapshot, Switchable) -> Unit): Switchable =
    AsyncObserver(f)
