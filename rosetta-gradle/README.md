# Rosetta Gradle plugins

Standalone build tooling for Rosetta and its consumers. Version 0.1.0 uses Gradle 9.6.1, Java 21, and Stonecutter 0.7.10. These plugins are build dependencies and never belong in a mod jar.

## Local use

Add `includeBuild("path/to/Rosetta_Library/rosetta-gradle")` inside `pluginManagement` in settings.gradle.kts. Rosetta uses its own included build; Via Romana defaults to the sibling checkout and accepts `-Prosetta.gradle.path=<path>` as an override. This does not change the local jar loader for runtime dependencies.

Apply `net.rasanovum.rosetta.stonecutter` alongside `dev.kikugie.stonecutter` in each controller script. The default profile is `common`; optional profiles are selected through:

```kotlin
rosettaStonecutter {
    profiles.set(setOf("common", "rendering", "renderingMethods", "clientAnnotations"))
}
```

- `common`: loader constants, mc_26, ResourceLocation/Identifier, GuiGraphics/GuiGraphicsExtractor.
- `rendering`: qualified Blaze3D/RenderPearl package migrations.
- `renderingMethods`: 26.2 rendering aliases. Opt in only for code using those Minecraft APIs, since method-name replacements are textual.
- `clientAnnotations`: loader-specific client annotations.

Profiles are read after controller evaluation and installed once per tree through Stonecutter's public parameters API. Chained rendering replacements are ordered differently for forward and reverse conversion. Active versions, target matrices, publishing, and project-specific replacements remain in the consumer.

## Shaders

Apply `net.rasanovum.rosetta.shaders` to target projects after resolving their properties. The plugin is available to Via Romana's subprojects through its root plugins block.

```kotlin
extra["rosetta.shaders.manifest"] = "map-foundation/shaders.json"
extra["rosetta.shaders.minecraft"] = prop("deps.minecraft")
apply(plugin = "net.rasanovum.rosetta.shaders")
```

The explicit Minecraft property is optional when deps.minecraft already contains the resolved version. A manifest is relative to the consumer root and declares sourceDirectory (also root-relative) plus shader paths relative to that resource directory:

```json
{
  "sourceDirectory": "src/main/resources",
  "shaders": {
    "assets/example/shaders/core/icon.fsh": {
      "inputs": { "texCoord0": 0, "vertexColor": 1 },
      "outputs": { "fragColor": 0 },
      "vanillaTransforms": true
    }
  }
}
```

For custom vertex/fragment pairs, set vertexShader on the fragment entry to the vertex resource path. Location/type pairs are checked between stages. Shader declarations remain explicit; the plugin does not infer locations from variable names. It supports the scalar/vector in/out declarations used by these shaders, including flat qualifiers.

The generated resources task tracks its inputs, writes under build/generated/rosetta-shaders, and replaces selected original resources even when Stonecutter has relocated them. It does not edit checked-in sources. 26.3 processing adds explicit locations and the separate shader objects extension, expands relative moj_import includes, converts namespaced imports to include, and optionally updates the known vanilla DynamicTransforms block layout. Missing/duplicate locations, mismatched stages, cyclic includes, and escaping includes fail the build. Earlier targets retain their GLSL content. Line endings are normalized to the host platform.

Legacy shader entries can set legacyOnly to true (pre-26). Set rosetta.shaders.legacyNamespace on the target to namespace their quoted imports where required by Forge. Modern pipeline resources remain unaffected by that legacy setting.
