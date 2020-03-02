
![GitHub Actions status](https://github.com/dennisschroeder/khome/workflows/Latest%20push/badge.svg)
![LINE](https://img.shields.io/badge/line--coverage-24%25-red.svg)
[![](https://jitpack.io/v/dennisschroeder/khome.svg)](https://jitpack.io/#dennisschroeder/khome)

# Khome

Khome is a smart home-automation library for **Home Assistant**, written in Kotlin. It makes heavy usage of the **Kotlin-DSL** 
for a good programming experience. This library let's you write your own application, that can listen to (state change) events 
and fires actions via the [Home Assistant Websocket API](https://developers.home-assistant.io/docs/en/external_api_websocket.html).
Or you can call any other third party code or API.

The heart of Khome is the [Ktor-Websocket-Client](https://ktor.io/clients/websockets.html). Khome uses ktor for the communication 
between your application and your Home-Assistant Server.

Simple Example:
```kotlin

class LivingRoomLuminance : AbstractSensorEntity("livingroom_luminance")
class LivingRoomLight : AbstractLightEntity("livingroom_main")
class TurnOnLivingRoomLight(light: LivingRoomLight) : TurnOn(light)

beans {
    bean { LivingRoomLuminance() }
    bean { LivingRoomLight() }
    service { TurnOnLivingRoomLight(get()) }
}

onStateChange<LivingRoomLuminance> { 
    if(stateValue <= 3.0) callService<TurnOnLivingRoomMoodLight>()
}
```

Khome is influenced by AppDeamon. AppDaemon is a loosely coupled, multithreaded, sandboxed, pluggable 
python execution environment for writing automation apps for Home-Assistant-home-automation software.

[AppDeamon@github](https://github.com/home-assistant/appdaemon) | [AppDeamon Documentation](https://appdaemon.readthedocs.io/en/latest/)

## Home Assistant
 
HA is an open-source home-automation platform written in Python 3 that puts local control and privacy first. Powered by 
a worldwide community of tinkerers and DIY enthusiasts. Perfect to run on a Raspberry Pi or a local server.

If you're not already familiar with Home Assistant, you find all you need on the [Getting Started page](https://www.home-assistant.io/getting-started/).

## Warning
This project is in early alpha state. You can't rely on it **yet**. But I encourage you to test it and report [issues](https://github.com/dennisschroeder/khome/issues).
Changes in the API, removal of features or other changes will occur. Of course, [contributions](https://github.com/dennisschroeder/khome/pulls) are also very welcome.

## If you are from...

#### ... the Kotlin World:
Since you Home Assistant is written in Python 3, you may ask yourself if you need to write Python code on the Home Assistant
side. But you don't have to. All you need to do is configuring it via `.yaml` files. But you need to install and run it on 
your own server. There is plenty of information and tutorials on the web to support you with that. [Google](https://google.com)
will help you. Also there is a [Discord channel](https://discordapp.com/invite/c5DvZ4e) to get in touch easily with the community.

#### ... the Python World:
Yes you need to learn Kotlin. It is definitely worth a try. In my personal opinion it is worth even more. But that's a different story.
Probably the fastest way for you to get into Kotlin is the [Kotlin for Python Introduction](https://kotlinlang.org/docs/tutorials/kotlin-for-py/introduction.html)
from the official Kotlin documentation. Here is a list of the most important [Kotlin online resources](https://kotlinlang.org/community/#kotlin-online-resources).

## Installation

#### Home Assistant
Further information on this topic is available on the official [Home Assistant Documentation](https://www.home-assistant.io/getting-started/) page.

#### Khome
For now you can use [Jitpack](http://jitpack.io) to install Khome locally. Just add the following lines to your `build.gradle` or maven file.
Since there is no official release yet, use `master-SNAPSHOT` as version. After the first release, exchange `master-SNAPSHOT` with 
the release tag.

#### Gradle
```groovy
repositories {
    // ...
    maven { url "https://jitpack.io" }
}
```
```groovy
dependencies {
    // ...
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
        <version>0.1.0-SNAPSHOT</version>
</dependency>

```

## Documentation

Khome has no opinion on how you want to run your application, what other libraries or pattern you choose or what else  is best for what you like to build.
All Khome needs is an Kotlin environment to run properly. All dependencies comes with it.

Again, if you are new to Kotlin, you might check out [Getting Started with Intellij IDEA](https://kotlinlang.org/docs/tutorials/getting-started.html)
or [Working with the Command Line Compiler](https://kotlinlang.org/docs/tutorials/command-line.html).
I recommend using Kotlin with Intellij IDEA to get started. It's the best way to get into it. You can download the free [Community Edition](http://www.jetbrains.com/idea/download/index.html) from JetBrains.

### Quickstart

#### Initialization & Configuration

To start listening to state change events and call services, you need to initialize and configure khome. Here you have three choices to configure khome

1. The functional way
```kotlin
khomeApplication { // this: Khome
    configure { // this: Configuration
        host = "localhost"
        port = 8123
        accessToken = "Your super secret token"
        secure = false
     }
}
```
2. Use dependency injection
Alternatively you can use a configuration bean.

```kotlin
data class MyConfiguration(
    override var host: String = "localhost",
    override var port: Int = 8123,
    override var accessToken: String = "Your super secret token"
    override var secure = false
) : ConfigurationInterface

khomeApplication { // this: Khome
    beans {
        bean<ConfigurationInterface>(override = true) { MyConfiguration() }
    }
}
```
3. Set environment variables
You can use env variables to configure home.
```.env
HOST=192.169.178.101
```

##### Required Parameters

- **host**: String <br> Local ip address or url from your Home-Assistant server instance

- **port**: Int <br> The port of Home-Assistant (defaults to 8123)

- **accessToken**: String <br> You need to create a [long-lived access token](https://developers.home-assistant.io/docs/en/auth_api.html#long-lived-access-token).
You can do so within the Lovelace ui. Just go to your user profile, scroll to the bottom and generate one.

- **secure**: Boolean <br> If you want to establish a secure websocket connection, you need to set this parameter to true (defaults to false).

#### Connect to the web socket api

```kotlin
khomeApplication {
    beans {/*...*/}
    connectAndRun { // this: KhomeSession
        
    }
}
```

By calling the connectAndRun() method, you establish a connection to the Home-Assistant websocket api, run the authentication process and start the state
change streaming. When all went as supposed, you should see the following output in the console. 

```bash
[main] INFO khome.core.Logger - Authentication required!
[main] INFO khome.core.Logger - Sending authentication message.
[main] INFO khome.core.Logger - Authenticated successfully.
```

#### React to state changes
No you can build your application with Khome by writing callbacks that get executed when a state change occurs.
```kotlin
class LivingRoomLuminance : AbstractSensorEntity("livingroom_luminance")
class LivingRoomLight : AbstractLightEntity("livingroom_main")
class TurnOnLivingRoomLight(light: LivingRoomMoodLight) : TurnOn(light)

khomeApplication {
    beans {
        bean { LivingRoomLuminance() }
        bean { LivingRoomLight() }
        bean { TurnOnLivingRoomLight(get()) }
    }
    connectAndRun { // this: KhomeSession
        onStateChange<LivingRoomLuminance> {
            if (stateValue <= 3) callService<TurnOnLivingRoomMoodLight>()
        }
    }
}
```


