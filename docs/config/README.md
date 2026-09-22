# Rosetta Config

Server-synchronized settings for MidnightLib. Handles sync between server and clients, restores previous local values upon leaving a server.

```text
net.rasanovum.rosetta:rosetta-config-<minecraft>-<loader>:0.2.0
```

Requires core and networking; MidnightLib is bundled by Config on every loader. See [setup](../setup.md).

Config's Maven POM exposes the matching MidnightLib API through `net.rasanovum.rosetta:midnightlib-<target>:<config-version>`. The API artifact is the same binary nested in Config, including any Rosetta-owned patches. Fabric and NeoForge consumers receive it transitively. Legacy Forge's non-transitive mod remapping requires an explicit `modImplementation` entry for that API coordinate; do not jar-in-jar it again. Consumers do not select a separate MidnightLib version or repository.

Publishing Config to the local Rosetta Maven repository also publishes this API artifact. Modified binaries, licenses, checksums, and refresh instructions live in [gradle/overrides](../../gradle/overrides/README.md).

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
