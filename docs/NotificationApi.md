# Notifications

Home Assistant has a powerful notification integration that serves a lot of [notification platforms](https://www.home-assistant.io/integrations#notifications) like Discord, Telegram, Slack, Facebook-Messenger, SMTP, SMS, the companion app for iOS and Android and much more.
Khome supports a variety of these notification platforms and the following list will even grow in the future. 


## Persistent Notifications
The [persistent_notification integration](https://www.home-assistant.io/integrations/persistent_notification/) can be used to show a notification on the frontend that has to be dismissed by the user.

You can create, dismiss and mark a notification as read.
To do so, check out this little examples:

### Create

```kotlin
val KHOME = khomeApplication()

KHOME.createPersistentNotification(
    title = "Exception in Observer",
    message = "Something bad happened!", 
    notificationId = "SomeIdentifier"
)
```
That's all! The title and notificationId argument are optional. If notificationId is given, it will overwrite the notification if there already was a notification with that same ID.

### Dismiss

```kotlin
KHOME.dismissPersistentNotification(id = "SomeIdentifier")
```
The `id` here is required and corresponds to the notificationId parameter in the create function, since Home-Assistant needs to know which notification to dismiss.
Dismissing the notification deletes it irrevocably.

### Mark as read
```kotlin
KHOME.markPersistentNotificationAsRead(id = "SomeIdentifier")
```
This does not delete the notification but visually dismisses it in the [user interface](https://www.home-assistant.io/lovelace/).



## Mobile App Notifications
Home Assistant comes with the so-called [companion app](https://companion.home-assistant.io/) for iOS and Android. Besides various other awesome features, it lets you receive [push notifications](https://companion.home-assistant.io/docs/notifications/notifications-basic) send via Home Assistant. 
Khome supports mobile phone notifications with the `KhomeApplication::notifyMobileApp` method which comes in 3 different flavors (signatures).

```kotlin
val KHOME = khomeApplication()

KHOME.notifyMobileApp(
    device = "mobile_app_myDevice", 
    title = "New message for you", 
    message = "This is a notification"
)
```
This lets you quickly send a message to the mobile device you selected. If you are not a big fan of stringly-typed applications, you can also
set the device parameter to an enum you created.

```kotlin
val KHOME = khomeApplication()

enum class MobilePhones {
    MY_DEVICE
}

KHOME.notifyMobileApp(
    device = MobilePhones.MY_DEVICE, 
    title = "New message for you", 
    message = "This is a notification"
)
```
The last signature of the method is EXPERIMENTAL and the API might change in the future. It DOES NOT WORK WITH ANDROID YET!
It uses function literal with a receiver to let you build a more advanced message to send to your mobile app.
Besides title and message, you can send data to your mobile app like attachments, sounds, and more.

### Sounds 
Adding a custom sound to a notification allows you to easily identify the notification without even looking at your device.
Home Assistant for iOS comes with some notification sounds pre-installed, but you can also upload your own.

[Companion App Documentation](https://companion.home-assistant.io/docs/notifications/notification-sounds)

```kotlin
KHOME.notifyMobileApp(MobilePhones.MY_DEVICE) {
    title = "New message for you"
    message = "This is a notification"
    
    data {
        sound("US-EN-Morgan-Freeman-Roommate-Is-Arriving.wav", critical = 1, volume = 1.0)
    }
}
```
The parameter `critical` and `volume` are optional and explained [here](https://companion.home-assistant.io/docs/notifications/critical-notifications).

### Attachments
Notifications may contain an image, video, or audio file attachment that is displayed alongside the notification. 
A thumbnail is shown on the notification preview and the full size attachment is displayed after the notification is expanded.
#### Static Attachments
[Companion App Documentation](https://companion.home-assistant.io/docs/notifications/notification-attachments)
```kotlin
KHOME.notifyMobileApp(MobilePhones.MY_DEVICE) {
    title = "New message for you"
    message = "This is a notification"
    
    data {
        attachment("https://upload.wikimedia.org/wikipedia/commons/thumb/6/6e/Home_Assistant_Logo.svg/1200px-Home_Assistant_Logo.svg.png")
    }
}
```
You can further configure your attachment, with two more parameters:
- content-type
- hide-thumbnail

See [companion app docs](https://companion.home-assistant.io/docs/notifications/notification-attachments#configuration) for more on this.

#### Dynamic Attachments
You can also attach dynamic content such as a map or a camera stream.

##### Map:
```kotlin
KHOME.notifyMobileApp(MobilePhones.MY_DEVICE) {
    title = "New message for you"
    message = "This is a notification"
    
    data {
        push {
            category = "map"
        }
        mapActionData {
            latitude = "40.785091"
            longitude = "-73.968285"
        }
    }
}
```
There are some more options to configure inside the `::mapActionData` method. Check out the [companion app documentation](https://companion.home-assistant.io/docs/notifications/dynamic-content#map)
for all options.

##### Camera stream:

```kotlin
KHOME.notifyMobileApp(MobilePhones.MY_DEVICE) {
    title = "New message for you"
    message = "This is a notification"
    attachement()
    data {
        attachment(contentType = "jpeg")
        push {
            category = "camera"
        }
        entityId = EntityId("camera", "livingroom")
    }
}
```

### Actionable Notifications
Actionable notifications are a unique type of notification as they allow the user to add buttons to the notification which can then send an event to Home Assistant once clicked.
This event can then be used in an automation allowing you to perform a wide variety of actions.

Some useful examples of actionable notifications:

- A notification is sent whenever motion is detected in your home while you're away or asleep. A "Sound Alarm" action button is displayed alongside the notification, that when tapped, will sound your burglar alarm.
- Someone rings your front doorbell. You receive a notification with a live camera stream of the visitor outside along with action buttons to lock or unlock your front door.
- Receive a notification whenever your garage door opens with action buttons to open or close the garage.

Check out all the details at the [companion app documentation](https://companion.home-assistant.io/docs/notifications/actionable-notifications)

An actionable notification in Khome just looks like this:

```kotlin
KHOME.notifyMobileApp(MobilePhones.MY_DEVICE) {
    title = "New message for you"
    message = "This is a notification"
    data {
        push {
            category = "my_custom_category"
        }
    }
}
```
The important part here is the `category` which has to match the push categories configuration configured in Home Assistants configuration.yaml.
When an action is selected an event named `ios.notification_action_fired` for iOS and `mobile_app_notification_action` for Android will be emitted on the Home Assistant event bus.
You can subscribe to this event in Khome and process the event data to bring those actions into life. See the [Home Assistant events section](HomeAssistantEvents.md) in the Khome documentation
for further information.

### Request location updates
You can force a device to attempt to report its location by sending a special notification.
The notification is not visible to the device owner and only works when the app is running or in the background.
On success the sensor.last_update_trigger will change to "Push Notification".

```kotlin
KHOME.requestLocationUpdate(MobilePhones.MY_DEVICE)
```
**DANGER** <br>
While it is possible to create an automation in Khome to call this service regularly to update sensors, this is not 
recommended as doing this too frequently may have a negative impact on your device's battery life and health.

### By the way
You can also pass several device ids to all the mobile app functions, to send messages to several recipients, like so: 

```kotlin
KHOME.notifyMobileApp(
    MobilePhones.MY_DEVICE,
    MobilePhones.Another_Device, 
    title = "New message for you", 
    message = "This is a notification"
)
```
Make sure to pass the title and message parameter as named arguments.A function may have only one vararg parameter.
If there are other parameters following the vararg parameter, then the values for those parameters can be passed using the named argument syntax.




