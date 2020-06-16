### Quick start

#### Initialization & Configuration

Before you can start your Khome application, you need to initialize and configure it. Here you have two choices to do so:

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

## Sensors & Actuators
Khome differentiates between Sensors and Actuators.
