# Khome
Khome is an smart home automation library for **Home Assistant** build in Kotlin. It makes heavy usage of the **Kotlin-DSL** 
for an good programming experience. This library let's you listen to (state change) events and fire your actions via the 
Home Assistant Websocket Api. Or you can call any other third party code or api, since it is just an library to let you
write your perfect automation application. 

```
listenState("sensor.livingroom_luminance") {
    constrain {
        newState.get<Double>() <= 3.0
    }

    callback {
        callService {
            light {
                entityId = "light.livingroom_main"
                service = "turn_on"
            }
        }
    }
}
```

## Home Assistant
HA is an open source home automation that puts local control and privacy first. Powered by a worldwide community of tinkerers
and DIY enthusiasts. Perfect to run on a Raspberry Pi or a local server.