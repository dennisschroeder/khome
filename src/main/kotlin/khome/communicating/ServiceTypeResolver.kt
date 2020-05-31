package khome.communicating

typealias ServiceCallResolver<S> = (DesiredState<S>) -> ResolvedServiceCommand

interface ResolvedServiceCommand {
    val service: ServiceTypeIdentifier
    val serviceData: CommandDataWithEntityId ?
}

data class DefaultResolvedServiceCommand(
    override val service: ServiceTypeIdentifier,
    override val serviceData: CommandDataWithEntityId
): ResolvedServiceCommand
