# Shaders


Apply `net.rasanovum.rosetta.shaders` to target projects after resolving their properties. See [plugin setup](README.md).

```kotlin
extra["rosetta.shaders.manifest"] = "shaders.json"
extra["rosetta.shaders.minecraft"] = project.property("deps.minecraft").toString()
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
