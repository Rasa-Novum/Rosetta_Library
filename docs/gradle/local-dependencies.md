# Local dependencies

`net.rasanovum.rosetta.local-dependencies` uses local jars in place of Maven dependencies. Remapping and jar-in-jar declarations stay the same.

Follow [plugin setup](README.md), then apply it to each target project:

```kotlin
plugins { id("net.rasanovum.rosetta.local-dependencies") }
```

Place jars under `libs/local/<target>/<maven-group>/<artifact>.jar`:

```text
libs/local/
  26.3-fabric/
    net.rasanovum.rosetta/
      rosetta-26.3-fabric.jar
```

The target defaults to the Gradle project name. Jar filenames omit the dependency version. Keep dependency declarations in your build script; the plugin logs local overrides and uses your normal repositories for everything else.

## Options

These Gradle properties are optional:

| Property                  | Default                                       |
|---------------------------|-----------------------------------------------|
| `localDependencies`       | `true`                                        |
| `localDependenciesDir`    | `libs/local`, relative to the repository root |
| `localDependenciesTarget` | Gradle project name                           |

To build without local overrides:

```powershell
.\gradlew.bat build -PlocalDependencies=false
```

Apply the plugin before dependencies are resolved. For repositories serving overridden modules, use ordinary `content` filters rather than `exclusiveContent`, which would prevent Gradle from using the local jars.
