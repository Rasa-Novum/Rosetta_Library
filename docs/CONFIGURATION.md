# Rosetta Config

Server-synchronized settings for MidnightLib. Handles sync between server and clients, restores previous local values upon leaving a server.

```text
com.rasanovum.rosetta:rosetta-config-<minecraft>-<loader>:0.1.0
```

Requires `Rosetta` and `MidnightLib`

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
Use `@ServerSetting(permissionLevel = [0-4])` to require a specific level, setting it to `0` will allow any player to edit it.

### `requireCheats` (default: false)
The singleplayer owner can edit without cheats unless `requireCheats = true`. For example, `@ServerSetting(requireCheats = true)` requires level 2 even in singleplayer; `@ServerSetting(permissionLevel = 4, requireCheats = true)` requires level 4.

### `disabledDescription` (default: "")
To append a message to disabled entries, use `@ServerSetting(disabledDescription = "\n Example string.")`. This allows for a more obvious explanation to clients that it's managed by the server.

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


## Build

```powershell
.\gradlew.bat buildConfigArtifacts
```

JARs are written to `build/release-config`.