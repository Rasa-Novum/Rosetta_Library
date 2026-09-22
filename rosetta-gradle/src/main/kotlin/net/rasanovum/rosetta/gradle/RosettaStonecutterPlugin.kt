package net.rasanovum.rosetta.gradle

import dev.kikugie.stonecutter.controller.StonecutterControllerExtension
import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.SetProperty

private val legacyWidgetTypes = setOf("Button", "AbstractButton", "AbstractWidget", "Tooltip", "EditBox", "TextAndImageButton", "MultiLineTextWidget")

abstract class RosettaStonecutterExtension {
    abstract val profiles: SetProperty<String>
    abstract val widgetTypes: SetProperty<String>
    abstract val widgetAdapters: MapProperty<String, String>
}

class RosettaStonecutterPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val options = project.extensions.create("rosettaStonecutter", RosettaStonecutterExtension::class.java)
        options.profiles.convention(setOf("common"))
        options.widgetTypes.convention(legacyWidgetTypes)
        options.widgetAdapters.convention(emptyMap())
        project.plugins.withId("dev.kikugie.stonecutter") {
            val controller = project.extensions.getByType(StonecutterControllerExtension::class.java)
            project.afterEvaluate {
                controller.parameters {
                    val selected = options.profiles.get()
                    require(selected.all { it in setOf("common", "legacyGui", "legacy1182", "legacy1192Registries", "widgets", "rendering", "renderingMethods", "clientAnnotations") }) {
                        "Unknown Rosetta Stonecutter profile: $selected"
                    }
                    if ("common" in selected) common()
                    if ("legacy1182" in selected && "common" !in selected) legacy1182()
                    if ("widgets" in selected) widgets(options)
                    if ("legacyGui" in selected && "common" !in selected) legacyGui()
                    if ("legacy1192Registries" in selected) legacy1192Registries()
                    if (eval(node.metadata.version, ">=26.3")) {
                        if ("renderingMethods" in selected) renderingMethods()
                        if ("rendering" in selected) rendering()
                    } else {
                        if ("rendering" in selected) rendering()
                        if ("renderingMethods" in selected) renderingMethods()
                    }
                    if ("clientAnnotations" in selected) clientAnnotations()
                }
            }
        }
    }
}

private fun StonecutterBuildExtension.rename(version: String, old: String, new: String) {
    replacements.string { direction.set(eval(node.metadata.version, ">=$version")); replace(old, new) }
}

private fun StonecutterBuildExtension.common() {
    constants.match(node.metadata.project.substringAfterLast('-'), "fabric", "forge", "neoforge")
    legacyGui()
    legacy1182()
    val modern = eval(node.metadata.version, ">=26.1")
    constants.put("mc_26", modern)
    rename("26.1", "ResourceLocation", "Identifier")
    replacements.regex {
        direction.set(modern)
        replace("\\bGuiGraphics\\b", "GuiGraphicsExtractor", "\\bGuiGraphicsExtractor\\b", "GuiGraphics")
    }
}

private fun StonecutterBuildExtension.legacy1192Registries() {
    if (!eval(node.metadata.version, "<1.19.3")) return
    mapOf(
        "net.minecraft.core.registries.BuiltInRegistries" to "net.minecraft.core.Registry",
        "net.minecraft.core.registries.Registries" to "net.minecraft.core.Registry",
        "Registries.DIMENSION" to "Registry.DIMENSION_REGISTRY"
    ).forEach { (old, new) -> replacements.string { direction.set(true); replace(old, new) } }
    replacements.regex { direction.set(true); replace("\\bBuiltInRegistries\\b", "Registry", "\\bRegistry\\b", "BuiltInRegistries") }
    replacements.regex { direction.set(true); replace("\\bMapColor\\b", "MaterialColor", "\\bMaterialColor\\b", "MapColor") }
    replacements.string { direction.set(true); replace("org.joml.Vector4f", "com.mojang.math.Vector4f") }
}

private fun StonecutterBuildExtension.rendering() {
    val packages = linkedMapOf(
        "buffers.GpuBuffer" to "buffers.GpuBuffer",
        "buffers.GpuBufferSlice" to "buffers.GpuBufferSlice",
        "pipeline.RenderPipeline" to "pipeline.RenderPipeline",
        "pipeline.BindGroupLayout" to "pipeline.BindGroupLayout",
        "pipeline.BlendFunction" to "pipeline.BlendFunction",
        "pipeline.ColorTargetState" to "pipeline.ColorTargetState",
        "pipeline.DepthStencilState" to "pipeline.DepthStencilState",
        "shaders.UniformType" to "pipeline.UniformType",
        "platform.CompareOp" to "pipeline.CompareOp",
        "systems.CommandEncoder" to "commands.CommandEncoder",
        "systems.RenderPass" to "commands.RenderPass",
        "textures.GpuTextureView" to "textures.GpuTextureView",
        "textures.GpuSampler" to "textures.GpuSampler",
        "textures.AddressMode" to "textures.AddressMode",
        "textures.FilterMode" to "textures.FilterMode",
        "vertex.VertexFormat" to "vertex.VertexFormat",
        "PrimitiveTopology" to "pipeline.PrimitiveTopology",
        "GpuFormat" to "GpuFormat"
    )
    packages.forEach { (old, new) -> rename("26.3", "com.mojang.blaze3d.$old", "com.mojang.renderpearl.api.$new") }
}

private fun StonecutterBuildExtension.renderingMethods() {
    linkedMapOf(
        "VertexFormat.Mode" to "com.mojang.blaze3d.PrimitiveTopology",
        "VertexFormatElement.COLOR" to "DefaultVertexFormat.COLOR_SEMANTIC_NAME",
        ".getVertexFormat()" to ".getVertexFormatBinding(0)",
        ".getGameRenderState()" to ".gameRenderState()",
        ".getMainRenderTarget()" to ".gameRenderer.mainRenderTarget()",
        "GpuBuffer.MappedView" to "com.mojang.blaze3d.buffers.GpuBufferSlice.MappedView"
    ).forEach { (old, new) -> rename("26.2", old, new) }
}

private fun StonecutterBuildExtension.clientAnnotations() {
    val loader = node.metadata.project.substringAfterLast('-')
    val annotations = mapOf(
        "fabric" to "@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)",
        "forge" to "@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)",
        "neoforge" to "@net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)",
        "neoforge-modern" to "// Client-only."
    )
    val key = if (loader == "neoforge" && eval(node.metadata.version, ">=26.1")) "neoforge-modern" else loader
    val chosen = annotations.getValue(key)
    annotations.values.filter { it != chosen }.forEach { old ->
        replacements.string { direction.set(true); replace(old, chosen) }
    }
}

private fun StonecutterBuildExtension.widgets(options: RosettaStonecutterExtension) {
    if (!eval(node.metadata.version, "<1.20")) return
    val selectedTypes = options.widgetTypes.get()
    require(legacyWidgetTypes.containsAll(selectedTypes)) { "Unknown widget types: ${selectedTypes - legacyWidgetTypes}" }
    val adapters = selectedTypes.associate { type ->
        "net.minecraft.client.gui.components.$type" to "net.rasanovum.rosetta.client.gui.legacy.$type"
    } + options.widgetAdapters.get()
    adapters.forEach { (vanilla, legacy) ->
        replacements.regex {
            direction.set(true)
            val boundary = if (vanilla.endsWith(".")) "" else "\\b"
            replace("\\b${Regex.escape(vanilla)}$boundary", legacy, "\\b${Regex.escape(legacy)}$boundary", vanilla)
        }
    }
}

private fun StonecutterBuildExtension.legacyGui() {
    if (eval(node.metadata.version, "<1.20")) {
        replacements.string {
            direction.set(true)
            replace("net.minecraft.client.gui.GuiGraphicsExtractor", "com.mojang.blaze3d.vertex.PoseStack")
        }
        replacements.regex {
            direction.set(true)
            replace("\\bGuiGraphicsExtractor\\b", "PoseStack", "\\bPoseStack\\b", "GuiGraphicsExtractor")
        }
        replacements.string {
            direction.set(true)
            replace("net.minecraft.client.gui.GuiGraphics", "com.mojang.blaze3d.vertex.PoseStack")
        }
        replacements.regex {
            direction.set(true)
            replace("\\bGuiGraphics\\b", "PoseStack", "\\bPoseStack\\b", "GuiGraphics")
        }
        replacements.string { direction.set(true); replace("getGuiGraphics()", "getPoseStack()") }
    }
}

private fun StonecutterBuildExtension.legacy1182() {
    if (!eval(node.metadata.version, "<1.19")) return
    mapOf("literal" to "TextComponent", "translatable" to "TranslatableComponent").forEach { (factory, type) ->
        replacements.regex {
            direction.set(true)
            replace("\\b(?:net\\.minecraft\\.network\\.chat\\.)?Component\\.$factory\\(", "new net.minecraft.network.chat.$type(", "(?!)", "")
        }
    }
    replacements.regex {
        direction.set(true)
        replace("\\b(?:net\\.minecraft\\.network\\.chat\\.)?Component\\.empty\\(\\)", "new net.minecraft.network.chat.TextComponent(\"\")", "(?!)", "")
    }
    mapOf(
        "net.minecraftforge.event.level." to "net.minecraftforge.event.world.",
        "TickEvent.LevelTickEvent" to "TickEvent.WorldTickEvent",
        "ScreenEvent.Render." to "ScreenEvent.DrawScreenEvent.",
        "ConfigScreenHandler" to "ConfigGuiHandler",
        "ConfigScreenFactory" to "ConfigGuiFactory",
        "RenderGuiEvent" to "RenderGameOverlayEvent",
        "ClientPlayerNetworkEvent.LoggingIn" to "ClientPlayerNetworkEvent.LoggedInEvent",
        "ClientPlayerNetworkEvent.LoggingOut" to "ClientPlayerNetworkEvent.LoggedOutEvent"
    ).forEach { (modern, legacy) ->
        replacements.string { direction.set(true); replace(modern, legacy) }
    }
}
