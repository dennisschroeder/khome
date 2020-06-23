package khome.extending.actuators

import khome.communicating.CommandDataWithEntityId
import khome.communicating.EntityIdOnlyServiceData
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator

fun <S : State<*>, A : Attributes> Actuator<S, A>.callService(service: Enum<*>, parameterBag: CommandDataWithEntityId = EntityIdOnlyServiceData()) =
    callService(service.name, parameterBag)
