
![GitHub Actions status](https://github.com/dennisschroeder/khome/workflows/Latest%20push/badge.svg)
![LINE](https://img.shields.io/badge/line--coverage-9%25-red.svg)
[![](https://jitpack.io/v/dennisschroeder/khome.svg)](https://jitpack.io/#dennisschroeder/khome)

# Khome

Khome is a smart home automation library for **Home Assistant**, written in Kotlin. It enables you write your own **Home Assistant** automation applications, that can observe state changes, listen to events and much more.
Khome was written with safeness in mind. That means we wrote Khome with a fail first approach in Mind. See more about this in the "Safety´s first Section" (coming soon).

Simple Example:
```kotlin
val KHOME = khomeApplication()

val LivingRoomLuminance = KHOME.LuminanceSensor("livingRoom_luminance")
val LivingRoomMainLight = KHOME.SwitchableLight("livingRoom_main_light")

fun main() {
    val luminanceObserver = KHOME.SwitchableLightObserver { snapshot, _ ->
        if (snapshot.state.value < 3.0) LivingRoomMainLight.desiredState = SwitchableState(ON)
    }
        
    LivingRoomLuminance.attachObserver(luminanceObserver)

    KHOME.runBlocking()
}
```

In this little example, we observed the luminance sensor in the living room and when the measurement of the luminance drops under 3 lux, we change the state of the main light in the living room to ON.
As you can see here, Khome encourages you to think in states rather than services you have to call, to achieve what you want. This is less error prone and distinguishes Khome from most other automation libraries.

Khome comes with a lot of predefined factory functions and data classes for generic entity types but also with a low level api that let´s you develop your own special entities.

## Home Assistant
 
HA is an open-source home-automation platform written in Python 3 that puts local control and privacy first. Powered by 
a worldwide community of tinkerers and DIY enthusiasts. Perfect to run on a Raspberry Pi or a local server.

If you're not already familiar with Home Assistant, you find all you need on the [Getting Started page](https://www.home-assistant.io/getting-started/).

## Warning
This project is in early alpha state. You can't rely on it **yet**. But I encourage you to test it and report [issues](https://github.com/dennisschroeder/khome/issues).
Changes in the API, removal of features or other changes will occur. Of course, [contributions](https://github.com/dennisschroeder/khome/pulls) are also very welcome.

## If you are from...

#### ... the Kotlin World:
Since Home Assistant is written in Python 3, you may ask yourself if you need to write Python code on the Home Assistant
side. But you don't have to. All you need to do is configuring it via `.yaml` files and/or the user interface. But you need to install and run it on 
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

Khome has no opinion on how you want to run your application, what other libraries or pattern you choose or what else is best for what you like to build.
All Khome needs is a Kotlin environment to run properly.

Again, if you are new to Kotlin, you might check out [Getting Started with Intellij IDEA](https://kotlinlang.org/docs/tutorials/getting-started.html)
or [Working with the Command Line Compiler](https://kotlinlang.org/docs/tutorials/command-line.html).
I recommend using Kotlin with Intellij IDEA to get started. It's the best way to get into it. You can download the free [Community Edition](http://www.jetbrains.com/idea/download/index.html) from JetBrains.

### Quickstart

#### Initialization & Configuration

To start listening to state change events and call services, you need to initialize and configure khome. Here you have two choices to configure Khome

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

2. Set environment variables
Alternatively,you can use env variables to configure home.
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
val KHOME = khomeApplication()

fun main() {
    //...
    KHOME.runBlocking()
}
```
By calling the `KhomeApplication::runBlocking` method, you establish a connection to the Home-Assistant websocket api and run the start sequences like authentication, entity registration validation and so on.
When all went as supposed, you should see the following output in your console. 

```bash
[main] INFO Authenticator - Authentication required!
[main] INFO Authenticator - Sending authentication message.
[main] INFO Authenticator - Authenticated successfully to homeassistant version 0.111.0
[main] INFO ServiceStoreInitializer - Requested registered homeassistant services
[main] INFO ServiceStoreInitializer - Stored homeassistant services in local service store
[main] INFO EntityStateInitializer - Requested initial entity states
[main] INFO EntityRegistrationValidation - Entity registration validation succeeded
[main] INFO StateChangeEventSubscriber - Successfully started listening to state changes
```
