# Khome References

## 1. Initializing

### initialize {}
#### Synopsis
```kotlin
fun initialize(init: Khome.() -> Unit): Khome
```
The initialize function is used to instantiate the Khome class and defines the library scope.

#### Returns
An instance of Khome.

#### Parameters
    init: Khome.() -> Unit
Takes an extension-function which has access to the Khome class.
Inside the function you can configure khome and connect to the web socket api. 

#### Examples

```
initialize {
    configure {...}
    connect { //this: DefaultClientWebSocketSession
        ...
    }
}
```

### configure {}
#### Synopsis
```kotlin
fun configure(init: Configuration.() -> Unit)
```
The `configure {}` function is a member of the Khome class. Use it to configure
the Khome. See example below.

#### Returns
`Unit`

#### Parameters
    init: Configuration.() -> Unit
Takes an extension-function which has access to the config property of the Khome class.
The config property itself is the Configuration class. See [Configuration](#Configuration())

#### Examples

```kotlin
configure {
    host = "localhost"
    port = 8123
    accesToken = "My super secret token"
}
```

### Configuration()
#### Synopsis

```kotlin
data class Configuration(
    var host: String,
    var port: Int,
    var accessToken: String,
    var startStateStream: Boolean
)
```
#### Properties
- **host**: String <br> Local ip address or url from your Home-Assistant server instance. <br>Defaults: `"localhost"`

- **port**: Int <br> The port of Home-Assistant (defaults to 8123). <br>Default: `8123`

- **accessToken**: String <br> You need to create a [long-lived access token](https://developers.home-assistant.io/docs/en/auth_api.html#long-lived-access-token).
You can do so within the Lovelace ui. Just go to your user profile, scroll to the bottom and generate one.

- **startStateStream**: Boolean <br> If you only want to use Khome to send messages and don't want to react on state changes, set this to false to avoid an overhead. <br>Default: `true`


### 2. Listening
... coming soon.
### 3. Calling
... coming soon.
