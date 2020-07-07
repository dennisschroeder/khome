package khome.communicating

import khome.entities.State

typealias ServiceCommandResolverFunction<S> = (S) -> ResolvedServiceCommand

@Suppress("FunctionName")
fun <S : State<*>> ServiceCommandResolver(resolverFunction: ServiceCommandResolverFunction<S>): ServiceCommandResolver<S> =
    ServiceCommandResolverImpl(resolverFunction)

interface ServiceCommandResolver<S> {
    fun resolve(desiredState: S): ResolvedServiceCommand
}

internal class ServiceCommandResolverImpl<S>(private val resolverFunction: ServiceCommandResolverFunction<S>) : ServiceCommandResolver<S> {
    override fun resolve(desiredState: S): ResolvedServiceCommand = resolverFunction(desiredState)
}

interface ResolvedServiceCommand {
    var domain: String?
    val service: Enum<*>
    val serviceData: CommandDataWithEntityId?
}

data class DefaultResolvedServiceCommand(
    override var domain: String? = null,
    override val service: Enum<*>,
    override val serviceData: CommandDataWithEntityId
) : ResolvedServiceCommand
