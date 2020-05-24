package khome.communicating

typealias ServiceTypeResolver<S> = (DesiredState<S>) -> ServiceTypeIdentifier

enum class ServiceType : ServiceTypeIdentifier {
    TURN_ON, TURN_OFF
}

interface ServiceTypeIdentifier
