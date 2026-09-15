# Rosetta Config

Server-synchronized settings for MidnightLib. Handles sync between server and clients, restores previous local values upon leaving a server.

```text
net.rasanovum.rosetta:rosetta-config-<minecraft>-<loader>:0.2.0
```

Requires core, networking, and MidnightLib. See [setup](../setup.md).

## Usage

Add `@ServerSetting` to entries owned by the server:

```java
public class CommonConfig extends MidnightConfig {
    @Entry(min = 1)
    @ServerSetting
    public static volatile int radius = 2;

    @Entry
    public static boolean show_hint = true;
}

MidnightConfig.init(MOD_ID, CommonConfig.class);
```
### `permissionLevel` (default: 2)
Set `permissionLevel` to an integer from 0 to 4. For example, `@ServerSetting(permissionLevel = 4)` requires level 4. A value of 0 allows any player to edit the setting.

### `requireCheats` (default: false)
The singleplayer owner can edit without cheats unless `requireCheats = true`. For example, `@ServerSetting(requireCheats = true)` requires level 2 even in singleplayer; `@ServerSetting(permissionLevel = 4, requireCheats = true)` requires level 4.

### `disabledDescription` (default: "")
To append a message to disabled entries, use `@ServerSetting(disabledDescription = "\n Example string.")`. The message explains why the setting cannot be edited.

## Refresh hooks

Register the server hook during common initialization, after registering the config:
```java
net.rasanovum.rosetta.config.ConfigSync.onServerChange(MOD_ID, server -> refreshServerState(server));
```

Register the client hook during client initialization:
```java
net.rasanovum.rosetta.config.ConfigSync.ClientSync.onChange(MOD_ID, () -> refreshClientState());
```

Calling this on server also triggers the server refresh hook and broadcasts the server-managed values to clients. Receiving clients then run their client refresh hook.
```java
eu.midnightdust.lib.config.MidnightConfig.configInstances.get(MOD_ID).loadValuesFromJson();
```
