# Datapack JSON

Author recipes, advancements, and tags using **Minecraft 1.21.1 JSON and paths**. This baseline stays fixed when new output targets are added. The plugin supports a subset of that format, unimplemented fields and triggers require a native override.

The `net.rasanovum.rosetta.data` Gradle plugin generates resources for the explicitly supported targets: 1.20.1, 1.21.1, 26.1, 26.2, and 26.3. Unknown versions fail instead of inheriting an unverified schema. Loader choice affects override selection, ordinary vanilla JSON uses the Minecraft version.

## Applying the plugin


Apply `net.rasanovum.rosetta.data` to each target project. Define recipes, advancements, tags, and pack metadata once under `src/main/rosetta-data`, `generateRosettaData` produces the target resources during `processResources`.

```kotlin
plugins { id("net.rasanovum.rosetta.data") }
rosettaData {
    minecraftVersion.set("26.3")
    loader.set("fabric")
}
```

For applied scripts, put the plugin in the root plugins block with `apply false`, then use:

```kotlin
extra["rosetta.data.minecraft"] = project.property("deps.minecraft").toString()
apply(plugin = "net.rasanovum.rosetta.data")
```

The default source directory is relative to the consumer root, for shared Stonecutter sources. Override it through `rosettaData.sourceDirectory` or the root-relative `rosetta.data.source` bootstrap property. Minecraft defaults to `deps.minecraft`, loader defaults to the project name suffix. Explicit bootstrap properties are also available as `rosetta.data.minecraft` and `rosetta.data.loader`.


## Source layout

```text
src/main/rosetta-data/
  pack.json
  data/example/recipe/sticks.json
  data/example/advancement/root.json
  data/example/tags/block/path_block.json
```

Use the singular directory names from 1.21.1. Output goes to each target's `build/generated/rosetta-data`, wired into `processResources`. The source tree is never rewritten. Ordinary resources can coexist, but the same output path in both trees fails the build. No recipe or advancement is synthesized unless you define it.

Tasks track definitions, Minecraft version, and loader as cache inputs. Removing a definition removes its generated output on the next generation. JSON comments, duplicate keys, invalid paths, and unsupported fields fail with the source file and target in the message.

## Recipes

```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": ["#", "#"],
  "key": { "#": { "tag": "minecraft:planks" } },
  "result": { "id": "minecraft:stick", "count": 4 }
}
```

Supported types: `crafting_shaped`, `crafting_shapeless`, `smelting`, `blasting`, `smoking`, `campfire_cooking`, and `stonecutting`, in the vanilla namespace. Ingredients use `{"item":"minecraft:stone"}` or `{"tag":"minecraft:planks"}`, or nonempty lists of these objects. Lists containing tags work on 1.20.1 and 1.21.1, newer targets reject them because vanilla no longer supports that representation. Use a single tag or a target override in that case.

The plugin converts ingredient objects to strings for newer targets and handles recipe directories, result `item` versus `id`, and the old cooking/stonecutting result format. A legacy cooking recipe cannot produce a count other than one. Common category/group, shaped notification, and cooking experience/time fields are retained.

Custom serializers, smithing/transmute recipes, component-bearing results, and NBT require an explicit native target definition. Component payloads change independently of the enclosing recipe schema, this release does not guess their meaning.

## Advancements

```json
{
  "display": {
    "icon": { "id": "minecraft:oak_planks" },
    "title": { "text": "First planks" },
    "description": { "text": "Obtain two planks" },
    "background": "minecraft:textures/gui/advancements/backgrounds/stone.png"
  },
  "criteria": {
    "planks": {
      "trigger": "minecraft:inventory_changed",
      "conditions": {
        "items": [{ "items": "#minecraft:planks", "count": { "min": 2 } }]
      }
    }
  },
  "requirements": [["planks"]]
}
```

Supported triggers are `inventory_changed`, `tick`, and `impossible`. Inventory conditions support `items` predicates (IDs, ID lists, or a tag, with optional count bounds) and `slots` bounds. The plugin adapts the old item predicate tag/list form, icon field, background texture path, and advancement directory. Requirements are checked against criteria. Parent, rewards, and ordinary display settings remain available.

Text supports literal strings and basic text/translate components, including nested `with`/`extra` and styling. Complex predicates, entity/player conditions, component icons, interaction events in text, and other triggers need native target definitions. In particular, a renamed trigger that changed gameplay meaning is not silently substituted.

## Tags

```json
{ "replace": false, "values": ["minecraft:stone", { "id": "#example:optional", "required": false }] }
```

Use vanilla 1.21.1 registry folder names, such as `block`, `item`, and `damage_type`. The plugin handles the older directory names when generating 1.20.1 output. Values and optional references are preserved. The plugin does not verify registry existence or loader-specific custom tag semantics.

## Target overrides

Use vanilla 1.21.1 JSON plus optional top-level `$rosetta` metadata:

```json
{
  "type": "minecraft:crafting_shapeless",
  "ingredients": [{ "item": "minecraft:stone" }],
  "result": { "id": "minecraft:stone_button" },
  "$rosetta": {
    "overrides": {
      "1.20.1": { "group": "legacy_buttons" },
      "1.20.1-fabric": { "group": null }
    },
    "native": {
      "26.3": { "type": "example:custom_recipe", "custom_field": true }
    },
    "exclude": ["26.2-neoforge"]
  }
}
```

Selectors are an exact supported Minecraft version or that version followed by `-fabric`, `-forge`, or `-neoforge`. Version overrides apply first, then loader-specific overrides. `overrides` use the same 1.21.1 authoring format and are recursive merge patches applied before conversion: null deletes a field, arrays replace arrays. `native` supplies a complete target-native JSON body and bypasses schema conversion, it still uses the target directory mapping. Loader-specific native bodies replace version-wide native bodies. `exclude` omits the resource for the selected target. Metadata never enters the mod jar. Unsupported target selectors are errors.

Native definitions are an escape hatch, not a promise of runtime validity. Minecraft and the relevant mod serializer remain authoritative for their contents.

## Pack metadata

Optional `pack.json`:

```json
{ "description": "Example resources", "kind": "combined" }
```

Kinds are `combined` (default, for mod jars), `data`, and `resources`. Generated `pack.mcmeta` accounts for separate data/resource versions:

| Minecraft | Data | Resources |
| --- | --- | --- |
| 1.20.1 | 15 | 15 |
| 1.21.1 | 48 | 34 |
| 26.1 | 101.1 | 84.0 |
| 26.2 | 107.1 | 88.0 |
| 26.3 | 121.0 | 97.1 |

Combined packs advertise the range spanning both pack types. For 1.21.1 this uses `supported_formats`, modern targets use `min_format` and `max_format`. Single-purpose packs advertise only their own format. This range serves the two pack types in one target jar, it does not claim that the generated data works on every intervening Minecraft release. `pack.json` has no target override metadata, target format rules belong in the plugin.

## Notes

The plugin does not cover loot tables, worldgen, item models, arbitrary JSON transformations, or automatic registry validation (yet).
