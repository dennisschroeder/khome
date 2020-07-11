### Quick start

#### Initialization & Configuration

Before you can start your Khome application, you need to initialize and configure it. Here you have two choices to do so:

1. The functional way
```kotlin
khomeApplication { //this: Khome
    configure { //this: Configuration
        host = "localhost"
        port = 8123
        accessToken = "Your super secret token"
        secure = false
     }
}
```

2. Set environment variables <br>
Alternatively, you can use env variables to configure home.
```.env
HOST=192.169.178.101
```

##### Required Parameters

- **host**: String <br> 
    Local ip address or url from your Home-Assistant server instance

- **port**: Int <br> 
    The port of Home-Assistant (defaults to 8123)

- **accessToken**: String <br> 
    You need to create a [long-lived access token](https://developers.home-assistant.io/docs/en/auth_api.html#long-lived-access-token).
    You can do so within the Lovelace UI. Just go to your user profile, scroll to the bottom, and generate one.

- **secure**: Boolean <br> 
    If you want to establish a secure WebSocket connection, you need to set this parameter to true (defaults to false).

#### Connect to the web socket API

```kotlin
val KHOME = khomeApplication()

fun main() {
    //...
    KHOME.runBlocking()
}
```
By calling the `KhomeApplication::runBlocking` method, you establish a connection to the Home-Assistant WebSocket API and run the start sequences like authentication, entity registration validation, and so on.
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

## Start writing your home automation application

Basically, a Khome application is a collection of observers attached to some entities. For your convenience, Khome comes with a lot of predefined entity types. 
For most uses cases, [here is all you need](PredefinedEntityTypes.md) to build your application. Since home assistant evolves rapidly and has the ability to be extended with custom integrations,
it comes along with a low-level API to build your own entities, based on your needs. You find more on that topic in the [Build your own entities](BuildEntitiesFromScratch.md) section.

The following examples to get you off and running are based on the [predefined entity types](PredefinedEntityTypes.md) and the [notification API](NotificationApi.md) provided by Khome.
For a deeper understanding of Khome's capabilities, we encourage you to read the [Sensors, Actuators, and Observer](SensorsAndActuators.md) section.

### Lower complexity

1. Turn on a light, when a motion sensor detects movement, and the sun is below horizon.

```kotlin
val KHOME = khomeApplication()

val HallwayLight = KHOME.DimmableLight("hallway_main")
val HallwayMotionSensor = KHOME.MotionSensor("hallway")
val Sun = KHOME.Sun()

fun main() {
    HallwayMotionSensor.attacheObserver {
        if (Sun.measurement.value == SunValue.BELOW_HORIZON) {
            when(measurement.value) {
                ON -> HallwayLight.desiredState = SwitchableState(ON)
                OFF -> HallwayLight.desiredState = SwitchableState(OFF)
            }
        }
    }
    KHOME.runBlocking()
}
```

2. Iterate over a list of covers and set them to a specific position when the sun has risen.

```kotlin
val KHOME = khomeApplication()
val Sun = KHOME.Sun()
val BedRoomCovers = listOf(
    KHOME.PositionableCover("bedroom_one"),
    KHOME.PositionableCover("bedroom_two"),
    KHOME.PositionableCover("bedroom_three"),
    KHOME.PositionableCover("bedroom_four"),
)

fun main() {
    Sun.attachObserver {
        if (history[1].state.value == SunValue.BELOW_HORIZON &&
            measurement.value == SunValue.ABOVE_HORIZON
        ) {
            for (cover in BedRoomCovers) {
                cover.desiredState = CoverState(value = CoverValue.OPEN, position = 60)
            }
        }
    }   
}
```

### Intermediate complexity

1. Send a notification to your mobile app when door sensor reports "door open" at night.

```kotlin
val KHOME = khomeApplication()

val GardenShedDoor = KHOME.ContactSensor("garden_shed")
val LateNight = KHOME.DayTime("late_night")

enum class MobilePhone {
    MY_PHONE
}

fun main() {
    GardenShedDoor.attachObserver {
        if (LateNight.measurement.value == SwitchableValue.ON &&
            history[1].state.value == ContactValue.CLOSED &&
            measurement.value == ContactValue.OPEN
        ) {
           KHOME.notifyMobileApp(MobilePhone.MY_PHONE) {
                title = "INTRUDER ALARM"
                message = "Garden shed door opened"
                data {
                    sound(critical = 1, volume = 1.0)
                }        
           }
        }
    }
}
```

### Higher complexity

1. When the Television got turned on, the livin groom covers get set to a specific position to comfortably watch some tv.
The previous positions get stored in a state store and when the tv got turned off, the covers get reset to the former positions.

```kotlin
val KHOME = khomeApplication()

val TelevisionLivingroom = KHOME.Television("tv_livingroom")
val ResetStateHistory = mutableMapOf<Cover,CoverState>()

val televisionWatchingCoverPosition = 
    KHOME.InputNumber("television_watching_cover_position").actualState.toInt()
val defaultCoverPosition = 75

val LivingRoomCovers = listOf(
    KHOME.PositionableCover("livingroom_one"),
    KHOME.PositionableCover("livingroom_two"),
    KHOME.PositionableCover("livingroom_three")
)

fun main() {
    TelevisionLivingroom.attachObserver {
        if (turnedOn) {
            for (cover in LivingRoomCovers) {
                ResetStateHistory[cover] = cover.actualState
                cover.desiredState = CoverState(CoverValue.OPEN, televisionWatchingCoverPosition)
            }
        }
        
        if (turnedOff) {
            for (cover in LivingRoomCovers) {
                cover.desiredState = ResetStateHistory[cover] ?: CoverState(CoverValue.OPEN, defaultCoverPosition)
            }
        }
    }   
}
```
