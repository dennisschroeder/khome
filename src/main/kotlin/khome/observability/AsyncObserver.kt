package khome.observability

import khome.KhomeApplication
import khome.core.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

typealias AsyncObserverSuspendable<S> = suspend CoroutineScope.(snapshot: WithHistory<State<S>>) -> Unit

fun<S> KhomeApplication.createAsyncObserver(f: AsyncObserverSuspendable<S>): Observer<State<S>> =
    AsyncObserver(f)

class AsyncObserver<S>(
    private val observer: AsyncObserverSuspendable<S>,
    context: CoroutineContext = Dispatchers.IO + DefaultEntityObserverExceptionHandler()
) : Observer<State<S>>, CoroutineScope by CoroutineScope(context) {
    override var enabled: Boolean = true
    override fun update(state: WithHistory<State<S>>) {
        if (enabled) launch { observer.invoke(this, state) }
    }
}
