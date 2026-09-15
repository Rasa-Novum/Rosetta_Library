plugins { id("dev.kikugie.stonecutter") }
stonecutter.active("26.1-fabric")
stonecutter {
    parameters {
        val loader = current.project.substringAfterLast('-')
        constants.match(loader, "fabric", "forge", "neoforge")

        constants.put("mc_26", eval(current.version, ">=26.1"))
        val legacyNames = !eval(current.version, ">=26.1")
        replacements.string {
            direction = legacyNames
            replace("net.minecraft.resources.Identifier", "net.minecraft.resources.ResourceLocation")
        }
        replacements.string {
            direction = legacyNames
            replace("Identifier", "ResourceLocation")
        }
        replacements.regex {
            direction = !legacyNames
            replace("\\bGuiGraphics\\b", "GuiGraphicsExtractor")
            reverse("\\bGuiGraphicsExtractor\\b", "GuiGraphics")
        }
    }
}
