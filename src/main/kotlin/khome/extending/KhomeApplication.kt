package khome.extending

import khome.KhomeApplication

fun <PB> KhomeApplication.callService(domain: Enum<*>, service: Enum<*>, parameterBag: PB) =
    callService(domain.name, service.name, parameterBag)
