package khome.communicating

import khome.entities.State
import khome.values.Domain
import khome.values.Service

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
    var domain: Domain?
    val service: Service
    val serviceData: CommandDataWithEntityId?
}

data class DefaultResolvedServiceCommand(
    override var domain: Domain? = null,
    override val service: Service,
    override val serviceData: CommandDataWithEntityId
) : ResolvedServiceCommand
