# Resources

`rosetta-resources` edits resources while Minecraft loads them. It requires core; see [setup](../setup.md). Its Java API is in `net.rasanovum.runeweaver`.

Use the [data Gradle plugin](../gradle/data.md) for static recipes, advancements, and tags. Use this artifact when a resource needs to change at runtime.

## API

- `Runeweaver.registerEvent(...)` registers an edit with a resource predicate, callback, priority, lifetime, and error policy.
- `RuneweaverEventBuilder<T>` provides the same options through setters.
- `RuneweaverCodecs` supplies codecs for JSON, PNG, and NBT resources.
- `Runeweaver.registerReference(...)` captures a resource for another operation to read.

A JSON event callback receives `EventContext<JsonElement>`. Read the value with `getFile()`, replace it with `setFile(...)`, or edit the JSON object directly. `getIndex()` identifies the resource. `createResource(...)` adds a resource, and `markForDeletion(true)` removes the current one.

Register persistent edits during mod initialization with `Lifetime.PERSISTENT`. Pass an explicit `ErrorPolicy` to choose how failures are handled.

See [Runeweaver](../../artifacts/resources/src/main/java/net/rasanovum/runeweaver/Runeweaver.java), [EventContext](../../artifacts/resources/src/main/java/net/rasanovum/runeweaver/EventContext.java), and [the event builder](../../artifacts/resources/src/main/java/net/rasanovum/runeweaver/RuneweaverEventBuilder.java) for signatures.
