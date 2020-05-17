package khome.observability

import khome.core.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

typealias AsyncObserverSuspendable<S> = suspend CoroutineScope.(snapshot: WithHistory<State<S>>, disabler: () -> Unit) -> Unit

class AsyncObserver<S>(
    private val observer: AsyncObserverSuspendable<S>,
    context: CoroutineContext = Dispatchers.IO + DefaultEntityObserverExceptionHandler()
) : Observer<State<S>>, CoroutineScope by CoroutineScope(context) {
    override var enabled: Boolean = true
    private val disabler = { enabled = false }
    override fun update(state: WithHistory<State<S>>) {
        if (enabled) launch { observer.invoke(this, state, disabler) }
    }
}
