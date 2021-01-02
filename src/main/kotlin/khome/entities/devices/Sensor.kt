package khome.entities.devices

import khome.entities.Attributes
import khome.entities.State
import khome.observability.Observable
import khome.observability.StateAndAttributes
import khome.observability.WithAttributes
import khome.observability.WithHistory

/**
 * An Sensor holding entity state and attributes
 *
 * In Khome, the Sensor is the immutable representation of an entity in home assistant.
 *
 * @param S the type of the state object that represents all state values of the entity. Has to implement the [State] interface.
 * @param A the type of the attributes object that represents all attribute values of the entity. Has to implement the [Attributes] interface.
 */
interface Sensor<S : State<*>, A : Attributes> : Observable<Sensor<S, A>>, WithHistory<StateAndAttributes<S, A>>, WithAttributes<A> {
    /**
     * Represents the current state object of the entity in Khome.
     */
    val measurement: S

    /**
     * Number of observers attached to the sensor.
     */
    val observerCount: Int

    /**
     * Represents the current attributes of the entity in Khome.
     */
    override val attributes: A
}
