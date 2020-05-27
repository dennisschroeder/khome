package khome.communicating

typealias ServiceTypeResolver<S> = (DesiredState<S>) -> ServiceTypeIdentifier
