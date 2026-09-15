# Stonecutter

Apply `net.rasanovum.rosetta.stonecutter` alongside `dev.kikugie.stonecutter` in each controller script. The default profile is `common`; optional profiles are selected through:

```kotlin
rosettaStonecutter {
    profiles.set(setOf("common", "rendering", "renderingMethods", "clientAnnotations"))
}
```

- `common`: loader constants, mc_26, ResourceLocation/Identifier, GuiGraphics/GuiGraphicsExtractor.
- `rendering`: Blaze3D/RenderPearl package migrations.
- `renderingMethods`: 26.2 rendering aliases.
- `clientAnnotations`: loader-specific client annotations.


Keep target versions and project-specific replacements in the consumer build. See [plugin setup](README.md).
