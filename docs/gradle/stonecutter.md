# Stonecutter

Rosetta's controller plugin and its consumers use Stonecutter **0.9.7**.

```kotlin
plugins {
    id("dev.kikugie.stonecutter")
    id("net.rasanovum.rosetta.stonecutter")
}

rosettaStonecutter {
    profiles.set(setOf("common", "widgets", "rendering"))
}
```

The default profile is `common`:

- `common`: loader constants, mc_26, ResourceLocation/Identifier, GuiGraphics/GuiGraphicsExtractor, and the legacy GUI rules below.
- `legacyGui`: before 1.20, maps GuiGraphics/GuiGraphicsExtractor to PoseStack and the GUI context accessor. Select separately when a consumer already owns its modern GUI transitions.
- `legacy1192Registries`: pre-1.19.3 registry imports/keys, material colors, and Vector4f names.
- `widgets`: before 1.20, maps qualified Button, AbstractButton, AbstractWidget, Tooltip, EditBox, TextAndImageButton and MultiLineTextWidget types to Rosetta core's legacy adapters. Requires the matching Rosetta core dependency.
- `rendering`: Blaze3D/RenderPearl package migrations.
- `renderingMethods`: 26.2 rendering aliases.
- `clientAnnotations`: loader-specific client annotations.
- `vanillaPackages`: vanilla RenderType and Util package moves for 26.1.
- `guiMethods`: opt-in GUI render/extract overrides and superclass/background calls. This is a source-convention profile for GuiGraphics methods, not a general Java method rewriter. Native input signature changes belong in typed GUI adapters.

## Widget selection and consumer adapters

The plugin owns version gating and replacement registration. Consumers can select built-in widget types and declare mappings for adapters they own:

```kotlin
rosettaStonecutter {
    profiles.set(setOf("legacyGui", "legacy1192Registries", "widgets"))
    widgetTypes.set(setOf("Button", "Tooltip", "TextAndImageButton", "MultiLineTextWidget"))
    widgetAdapters.putAll(mapOf(
        "net.minecraft.client.gui.components.tabs." to "eu.midnightdust.lib.legacy.LegacyTabs."
    ))
}
```

By default, all built-in widget types are selected. An explicitly empty `widgetTypes` set disables built-in mappings while retaining any `widgetAdapters`. Unknown names fail configuration.

Keep target versions and project-specific replacements in the consumer build. See [plugin setup](README.md).
