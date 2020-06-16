package khome.core

/**
 * A data class representing home assistants error response details
 *
 * @property code error code returning from home assistant
 * @property message error message returning from home assistant
 */
data class ErrorResponse(val code: String, val message: String)
