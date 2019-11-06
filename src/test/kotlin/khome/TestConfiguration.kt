package khome

import khome.core.Configuration

class TestConfiguration : Configuration() {
    override var host: String = "192.168.178.22"
    override var port: Int = 8123
    override var accessToken: String = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
        ".eyJpc3MiOiI3ZWExYWVlYzMyMDQ0NmI1ODI4YTNjM" +
        "WFlZWFhODg0YSIsImlhdCI6MTU0OTEzNjg2OCwiZXh" +
        "wIjoxODY0NDk2ODY4fQ.WzGYf5CYd2NJRD95gXS_Ou" +
        "5hlQ3T9QmIoxoPAxyLqGs"
    override var logLevel = "DEBUG"
}

