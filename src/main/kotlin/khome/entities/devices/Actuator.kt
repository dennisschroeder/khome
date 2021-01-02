package khome.entities.devices

import khome.communicating.CommandDataWithEntityId
import khome.communicating.EntityIdOnlyServiceData
import khome.entities.Attributes
import khome.entities.State
import khome.observability.Observable
import khome.observability.StateAndAttributes
import khome.observability.WithAttributes
import khome.observability.WithHistory
import khome.values.Service

/**
 * An Actuator holding entity state and attributes
 *
 * In Khome, the Actuator is the mutable representation of an entity in home assistant.
 *
 * @param S the type of the state object that represents all mutable state values of the entity. Has to implement the [State] interface.
 * @param A the type of the attributes object that represents all immutable attribute values of the entity. Has to implement the [Attributes] interface.
 */
interface Actuator<S : State<*>, A : Attributes> : Observable<Actuator<S, A>>, WithHistory<StateAndAttributes<S, A>>, WithAttributes<A> {
    /**
     * Represents the current state object of the entity in Khome.
     * Holds all state values that can be mutated directly.
     */
    val actualState: S

    /**
     * Represents the current attributes of the entity in Khome
     * Holds all state attributes that can not directly be mutated.
     */
    override var attributes: A

    /**
     * Set this property to a desired version of the state to mutate it in home assistant.
     * The setter of this property will interfere the setting, and translates the new (desired) state
     * to an service command that mutates the state in home assistant.
     */
    var desiredState: S?

    /**
     * Number of observers attached to the actuator.
     */
    val observerCount: Int

    /**
     * Sends a service command over the Websocket API to home assistant
     *
     * @param service the name of the action/service to execute
     * @param parameterBag the service parameter object. Has to inherit [CommandDataWithEntityId].
     */
    fun callService(service: Service, parameterBag: CommandDataWithEntityId = EntityIdOnlyServiceData())
}
