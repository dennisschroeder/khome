package khome.extending.entities.actuators.inputs

import khome.communicating.DesiredServiceData

data class SettableStateValueServiceData<S>(private val value: S) : DesiredServiceData()
