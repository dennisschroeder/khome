package khome.core.dependencyInjection

import khome.Khome
import khome.KhomeSession
import khome.calling.PersistentNotificationCreate
import khome.core.boot.BootSequenceInterface
import khome.core.entities.system.DateTime
import khome.core.entities.system.Sun
import khome.core.entities.system.Time

internal class KhomeModulesInitializer(override val khomeSession: KhomeSession) :
    BootSequenceInterface {

    private val systemBeansModule =
        khomeModule(createdAtStart = true, override = true) {
            bean { Sun() }
            bean { Time() }
            bean { DateTime() }
            service { PersistentNotificationCreate() }
        }

    private val userBeansModule =
        khomeModule(
            createdAtStart = true,
            moduleDeclaration = Khome.beanDeclarations
        )

    override suspend fun runBootSequence() {
        loadKhomeModule(systemBeansModule)
        loadKhomeModule(userBeansModule)
    }
}
