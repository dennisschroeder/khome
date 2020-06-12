package khome.communicating

/**
 * Main entry point to create new service enum classes
 */
interface ServiceTypeIdentifier

enum class ServiceType : ServiceTypeIdentifier {
    TURN_ON, TURN_OFF, SET_COVER_POSITION, OPEN_COVER, CLOSE_COVER, STOP_COVER, SET_VALUE, SELECT_OPTION, SET_DATETIME
}
