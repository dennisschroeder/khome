
![GitHub Actions status](https://github.com/dennisschroeder/khome/workflows/Latest%20push/badge.svg)
![LINE](https://img.shields.io/badge/line--coverage-7%25-red.svg)
[![](https://jitpack.io/v/dennisschroeder/khome.svg)](https://jitpack.io/#dennisschroeder/khome)

# Khome

Khome is a smart home automation library for **Home Assistant**, written in Kotlin. It enables you to write your own **Home Assistant** automation applications, that can observe state changes, listen to events, and much more.
Khome was written with safeness in mind. That means we wrote Khome with a fail first approach. See more about this in the ["Safety's first Section"](docs/Safety'sFirst.md).

Simple Example:
```kotlin
val KHOME = khomeApplication()

val LivingRoomMotion = KHOME.LuminanceSensor("livingRoom_motion")
val LivingRoomMainLight = KHOME.SwitchableLight("livingRoom_main_light")

fun main() {
    LivingRoomMotion.attachObserver { //this: Sensor<SwitchableState,MotionSensorAttributes>
        if (measurement.value == SwitchableValue.ON) {
            LivingRoomMainLight.desiredState = SwitchableState(ON)
        }
    }

    KHOME.runBlocking()
}
```

In this little example, we observed the motion sensor in the living room and when the sensors motion alarm turns to on, we change the state of the main light in the living room to ON.
As you can see here, Khome encourages you to think in states rather than services you have to call. This is less error-prone and helps the developer to stay in the mindset of states. This distinguishes Khome from most other automation libraries.

Khome comes with a lot of predefined factory functions, data classes, observers and more for generic entity types.
To achieve the same result than shown above, we can also use Khomes higher-level API which lets you write concise code.

```kotlin
val KHOME = khomeApplication()

val LivingRoomMotion = KHOME.LuminanceSensor("livingRoom_motion")
val LivingRoomMainLight = KHOME.SwitchableLight("livingRoom_main_light")

fun main() {
    LivingRoomMotion.onMotionAlarm { //this: MotionSensor
        LivingRoomMainLight.turnOn()
    }

    KHOME.runBlocking()
}
```

## Home Assistant
 
HA is an open-source home-automation platform written in Python 3 that puts local control and privacy first. Powered by
a worldwide community of tinkerers and DIY enthusiasts. Perfect to run on a Raspberry Pi or a local server.

If you're not already familiar with Home Assistant, you find all you need on the [Getting Started page](https://www.home-assistant.io/getting-started/).

## Warning
This project is in an early state. You can't rely on it **yet**. But I encourage you to test it and report [issues](https://github.com/dennisschroeder/khome/issues).
Changes in the API, removal of features, or other changes will occur. Of course, [contributions](https://github.com/dennisschroeder/khome/pulls) are also very welcome.

## If you are from...

#### ... the Kotlin World:
Since Home Assistant is written in Python 3, you may ask yourself if you need to write Python code on the Home Assistant
side. But you don't have to. All you need to do is configuring it via `.yaml` files and/or the user interface. But you need to install and run it on your own server. There is plenty of information and tutorials on the web to support you with that. [Google](https://google.com)
will help you. Also, there is a [Discord channel](https://discordapp.com/invite/c5DvZ4e) to get in touch easily with the community.

#### ... the Python World:
Yes, you need to learn Kotlin. It is definitely worth a try. In my opinion, it is worth even more. But that's a different story. Probably the fastest way for you to get into Kotlin is the [Kotlin for Python Introduction](https://kotlinlang.org/docs/tutorials/kotlin-for-py/introduction.html)
from the official Kotlin documentation. Here is a list of the most important [Kotlin online resources](https://kotlinlang.org/community/#kotlin-online-resources).

## Installation

#### Home Assistant
Further information on this topic is available on the official [Home Assistant Documentation](https://www.home-assistant.io/getting-started/) page.

#### Khome
For now, you can use [Jitpack](http://jitpack.io) to install Khome locally. Just add the following lines to your `build.gradle` or maven file.

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
    implementation 'com.github.dennisschroeder:khome:${replace with a version}'
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
        <version>${replace with a version}</version>
</dependency>

```

## Documentation

Khome has no opinion on how you want to run your application, what other libraries or pattern you choose, or what else is best for what you like to build. All Khome needs is a Kotlin environment to run properly.

Again, if you are new to Kotlin, you might check out [Getting Started with IntelliJ IDEA](https://kotlinlang.org/docs/tutorials/getting-started.html)
or [Working with the Command Line Compiler](https://kotlinlang.org/docs/tutorials/command-line.html).
I recommend using Kotlin with Intellij IDEA to get started. It's the best way to get into it. You can download the free [Community Edition](http://www.jetbrains.com/idea/download/index.html) from JetBrains.

### Working with Khome
- [Quick start](docs/Quickstart.md)
- [Sensors, Actuators and Observers](docs/SensorsAndActuators.md)
- [Home Assistant Events](docs/HomeAssistantEvents.md) (coming soon)
- [Predefined entity types](docs/PredefinedEntityTypes.md) (to be finished)
- [Build entity types from scratch](docs/BuildEntitiesFromScratch.md) (coming soon)
- [Notifications API](docs/NotificationApi.md)
- [Safety's first](docs/Safety'sFirst.md)


## Credits
Even though the idea to build Khome and the implementation was made by [me](https://github.com/dennisschroeder), you'll find the word "we" quite often in the documentation.
That is because I had the pleasure to work with [Tobias Hermann](https://github.com/Dobiasd) on this project who served me as a sparring partner by challenging my ideas and design decisions, 
introducing new ideas and designs, reviewing some parts of the code, and an overall consultant in the field of software engineering. So using I in the documentation would
just be incorrect.

Therefore, a special thank goes out to [Tobias](https://github.com/Dobiasd). 

Check out his awesome work, especially [frugally-deep](https://github.com/Dobiasd/frugally-deep), [functionalPlus](https://github.com/Dobiasd/FunctionalPlus), and his [thoughts on programming](https://github.com/Dobiasd/articles).
Oh and leave a star or two ;-). 
