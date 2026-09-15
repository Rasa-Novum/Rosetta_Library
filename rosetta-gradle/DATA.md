# Define datapack JSON once

The `net.rasanovum.rosetta.data` Gradle plugin generates resources for the explicitly supported targets: 1.20.1, 1.21.1, 26.1, 26.2, and 26.3. Unknown versions fail instead of inheriting an unverified schema. Loader choice affects override selection; ordinary vanilla JSON uses the Minecraft version.

## Source layout

```text
src/main/rosetta-data/
  pack.json
  data/example/recipe/sticks.json
  data/example/advancement/root.json
  data/example/tags/block/path_block.json
```

Use singular canonical directories. Output goes to each target's `build/generated/rosetta-data`, wired into `processResources`. The source tree is never rewritten. Ordinary resources can coexist, but the same output path in both trees fails the build. No recipe or advancement is synthesized unless you define it.

Tasks track definitions, Minecraft version, and loader as cache inputs. Removing a definition removes its generated output on the next generation. JSON comments, duplicate keys, invalid paths, and unsupported fields fail with the source file and target in the message.

## Recipes

```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": ["#", "#"],
  "key": { "#": "#minecraft:planks" },
  "result": { "id": "minecraft:stick", "count": 4 }
}
```

Supported types: `crafting_shaped`, `crafting_shapeless`, `smelting`, `blasting`, `smoking`, `campfire_cooking`, and `stonecutting`, in the vanilla namespace. Ingredients are namespaced item strings, `#namespace:tag` strings, or nonempty lists of item strings. Lists containing tags are rejected because newer Minecraft cannot represent them as vanilla ingredient alternatives.

The plugin handles legacy ingredient objects, recipe directories, result `item` versus `id`, and the old cooking/stonecutting result format. A legacy cooking recipe cannot produce a count other than one. Common category/group, shaped notification, and cooking experience/time fields are retained.

Custom serializers, smithing/transmute recipes, component-bearing results, and NBT require an explicit native target definition. Component payloads change independently of the enclosing recipe schema; this release does not guess their meaning.

## Advancements

```json
{
  "display": {
    "icon": { "id": "minecraft:oak_planks" },
    "title": { "text": "First planks" },
    "description": { "text": "Obtain two planks" },
    "background": "minecraft:gui/advancements/backgrounds/stone"
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

Canonical registry folders `block`, `item`, `fluid`, `entity_type`, `game_event`, and `function` are pluralized for 1.20.1. Other registry paths, such as `damage_type`, retain their names. Values and optional references are preserved. The plugin does not verify registry existence or loader-specific custom tag semantics.

## Target overrides

Use ordinary canonical JSON plus optional top-level `$rosetta` metadata:

```json
{
  "type": "minecraft:crafting_shapeless",
  "ingredients": ["minecraft:stone"],
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

Selectors are an exact supported Minecraft version or that version followed by `-fabric`, `-forge`, or `-neoforge`. Version overrides apply first, then loader-specific overrides. `overrides` are recursive merge patches applied before conversion: null deletes a field, arrays replace arrays. `native` supplies a complete target-native JSON body and bypasses schema conversion; it still uses the target directory mapping. Loader-specific native bodies replace version-wide native bodies. `exclude` omits the resource for the selected target. Metadata never enters the mod jar. Unsupported target selectors are errors.

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

Combined packs advertise the range spanning both pack types. For 1.21.1 this uses `supported_formats`; modern targets use `min_format` and `max_format`. Single-purpose packs advertise only their own format. This range serves the two pack types in one target jar; it does not claim that the generated data works on every intervening Minecraft release. `pack.json` has no target override metadata; target format rules belong in the plugin.

## Format evidence and boundaries

- [Minecraft 1.20.5](https://www.minecraft.net/en-us/article/minecraft-java-edition-1-20-5): item stacks, icons, and item predicate changes.
- [Minecraft 1.21](https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21): singular data directories.
- [Minecraft 1.21.2](https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-2): string ingredients and restrictions on alternatives.
- [Minecraft 1.21.5](https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-5): advancement background IDs.
- [Minecraft 26.1](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-1) and [26.2](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-2): pack format versions.
- 26.3: Mojang's client jar `version.json` and bundled vanilla recipes/advancements, inspected locally.

This first release deliberately does not cover loot tables, worldgen, item models, arbitrary JSON transformations, or automatic registry validation. Add verified adapters as concrete consumers need them. Generation checks structure and known conversion boundaries; it is not a substitute for loading a datapack in Minecraft.
