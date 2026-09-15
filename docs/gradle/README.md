# Gradle plugins

Rosetta provides four build-time plugins:

| Plugin | Guide |
| --- | --- |
| `net.rasanovum.rosetta.stonecutter` | [Shared source replacements](stonecutter.md) |
| `net.rasanovum.rosetta.shaders` | [Shader processing](shaders.md) |
| `net.rasanovum.rosetta.local-dependencies` | [Local development jars](local-dependencies.md) |
| `net.rasanovum.rosetta.data` | [Datapack JSON](data.md) |

## Setup

Add the plugin build to `settings.gradle.kts`:

```kotlin
pluginManagement {
    includeBuild("path/to/Rosetta_Library/rosetta-gradle")
}
```

Apply the plugins you need in your build scripts. For an applied script, declare its plugin in the root `plugins` block with `apply false` before calling `apply(plugin = "...")`.

The plugin build uses Java 21 and Stonecutter 0.7.10. It is built with Gradle 9.6.1. Plugins are build dependencies and are not bundled in mod jars.
