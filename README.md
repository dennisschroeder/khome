# Khome
Khome is an smart home automation library for **Home Assistant**  written in Kotlin. It makes heavy usage of the **Kotlin-DSL** 
for an good programming experience. This library let's you write your own application, that can listen to (state change) events 
and fires actions via the [Home Assistant Websocket Api](https://developers.home-assistant.io/docs/en/external_api_websocket.html).
Or you can call any other third party code or api.

Example:
```kotlin
listenState("sensor.livingroom_luminance") {
    constrain {
        newState.get<Double>() ?: 0.0 <= 3.0
    }

    action {
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
Changes in the API, removal of features or other changes will occur.

## Installation
For now you can use [Jitpack](http://jitpack.io) to install Khome locally. Just add the following lines to your build.gradle file.
Since there is no official release yet, use `master-SNAPSHOT` as version. After the first release, exchange `master-SNAPSHOT` with 
the release tag.

#### Gradle
```groovy
repositories {
    ...
    maven { url "https://jitpack.io" }
}
```
```groovy
dependencies {
    ...
    implementation 'com.github.dennisschroeder:khome:master-SNAPSHOT'
}
```

#### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
```xml
<dependency>
        <groupId>com.github.dennisschroeder</groupId>
        <artifactId>khome</artifactId>
        <version>master-SNAPSHOT</version>
</dependency>

```