package khome.communicating

import khome.core.State

typealias ServiceCallResolverFunction<S> = (S) -> ResolvedServiceCommand

@Suppress("FunctionName")
fun<S : State<*>> ServiceCommandResolver(resolverFunction: ServiceCallResolverFunction<S>): ServiceCommandResolver<S> =
    ServiceCommandResolverImpl(resolverFunction)

interface ServiceCommandResolver<S> {
    fun resolve(desiredState: S): ResolvedServiceCommand
}

class ServiceCommandResolverImpl<S>(private val resolverFunction: ServiceCallResolverFunction<S>) : ServiceCommandResolver<S> {
    override fun resolve(desiredState: S): ResolvedServiceCommand = resolverFunction(desiredState)
}

interface ResolvedServiceCommand {
    val service: Enum<*>
    val serviceData: CommandDataWithEntityId?
}

data class DefaultResolvedServiceCommand(
    override val service: Enum<*>,
    override val serviceData: CommandDataWithEntityId
) : ResolvedServiceCommand
