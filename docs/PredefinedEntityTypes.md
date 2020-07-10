# Predefined components
For your convenience, Khome comes with a lot of predefined factory functions, data classes, observers and more for generic entity types.

## Helpers
Helpers in home assistant are part of the [automation integrations](https://www.home-assistant.io/integrations/#automation). Some of those are represented as entities, 
therefore you can use them in Khome.

### Input Boolean
The input_boolean integration allows the user to define boolean values that can be controlled via the frontend and can be 
used within conditions of automation. See [home assistant documentation](https://www.home-assistant.io/integrations/input_boolean/) for more.

| Element    | Name | Sourcecode | KDocs |
|------------|------|------------|-------|
| Factory    | InputBoolean   |  [Link](../src/main/kotlin/khome/extending/actuators/Helper.kt) | [Link](https://dennisschroeder.github.io/khome/khome/khome.extending.actuators/-input-boolean.html)      |
| State      | SwitchableState     |  [Link](../src/main/kotlin/khome/extending/DeviceStates.kt) | [Link](https://dennisschroeder.github.io/khome/khome/khome.extending/-switchable-state/index.html)      |
| Attributes | InputBooleanAttributes    |  [Link](../src/main/kotlin/khome/extending/DeviceAttributes.kt) | [Link](https://dennisschroeder.github.io/khome/khome/khome.extending/-input-boolean-attributes/index.html)      |

Example:
```kotlin
val KHOME: KhomeApplication = khomeApplication()
val SleepMode = KHOME.InputBoolean("sleep_mode")

fun main() { 
    SleepMode.attachObserver { //this:Actuator<SwitchableState,InputBooleanAttributes>
        if (actualState.value = SwitchableValue.ON) {
            //... turn off lights, close covers, activate the alarm, etc.
        }
    }
    KHOME.runBlocking()
}
```
### Input Number
The input_number integration allows the user to define values that can be controlled via the frontend and can be used within conditions of automation. 
The frontend can display a slider, or a numeric input box. Changes to the slider or numeric input box generate state events. 
These state events can be utilized as automation triggers as well. See [home assistant documentation](https://www.home-assistant.io/integrations/input_number/) for more.

...<br>
...<br>
...... coming soon!


### Input Text
The input_text integration allows the user to define values that can be controlled via the frontend and can be used within conditions of automation.
Changes to the value stored in the text box generate state events. These state events can be utilized as automation triggers as well.
It can also be configured in password mode (obscured text). See [home assistant documentation](https://www.home-assistant.io/integrations/input_text/) for more.

...<br>
...<br>
...<br>

### Input Select
The input_select integration allows the user to define a list of values that can be selected via the frontend and can be used within conditions of automation.
When a user selects a new item, a state transition event gets generated. This state event can be used in an automation trigger. See [home assistant documentation](https://www.home-assistant.io/integrations/input_select/) for more.

...<br>
...<br>
...<br>

### Input Datetime
The input_datetime integration allows the user to define date and time values that can be controlled via the frontend and can be used within automations and templates.
See [home assistant documentation](https://www.home-assistant.io/integrations/input_datetime/) for more.

#### InputTime
#### InputDate
#### InputDateTime

## Lights
...coming soon!

## Cover
...coming soon!

## ...
