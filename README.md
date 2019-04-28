# Khome
Khome is an smart home automation library for **Home Assistant**  written in Kotlin. It makes heavy usage of the **Kotlin-DSL** 
for an good programming experience. This library let's you write your own application, that can listen to (state change) events 
and fires actions via the [Home Assistant Websocket Api](https://developers.home-assistant.io/docs/en/external_api_websocket.html).
Or you can call any other third party code or api.

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

HA is an Open-Source Home-Automation-Platform written in python 3 that puts local control and privacy first. Powered by 
a worldwide community of tinkerers and DIY enthusiasts. Perfect to run on a Raspberry Pi or a local server.

If your not already familiar with Home Assistant, you find all you need on the [Getting Started](https://www.home-assistant.io/getting-started/) 
page.

## Warning
This project is in early alpha state. You can't rely on it **yet**. But I encourage everybody to test it and report issues.