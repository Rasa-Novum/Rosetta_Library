package net.rasanovum.rosetta.gradle

import dev.kikugie.stonecutter.controller.StonecutterControllerExtension
import dev.kikugie.stonecutter.build.param.StonecutterBuildProperties
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.SetProperty

abstract class RosettaStonecutterExtension {
    abstract val profiles: SetProperty<String>
}

class RosettaStonecutterPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val options = project.extensions.create("rosettaStonecutter", RosettaStonecutterExtension::class.java)
        options.profiles.convention(setOf("common"))
        project.plugins.withId("dev.kikugie.stonecutter") {
            val controller = project.extensions.getByType(StonecutterControllerExtension::class.java)
            project.afterEvaluate {
                controller.parameters {
                    val selected = options.profiles.get()
                    require(selected.all { it in setOf("common", "rendering", "renderingMethods", "clientAnnotations") }) {
                        "Unknown Rosetta Stonecutter profile: $selected"
                    }
                    if ("common" in selected) common()
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

private fun StonecutterBuildProperties.rename(version: String, old: String, new: String) {
    replacements.string { direction.set(eval(node.metadata.version, ">=$version")); replace(old, new) }
}

private fun StonecutterBuildProperties.common() {
    constants.match(node.metadata.project.substringAfterLast('-'), "fabric", "forge", "neoforge")
    val modern = eval(node.metadata.version, ">=26.1")
    constants.put("mc_26", modern)
    rename("26.1", "ResourceLocation", "Identifier")
    replacements.regex {
        direction.set(modern)
        replace("\\bGuiGraphics\\b", "GuiGraphicsExtractor")
        reverse("\\bGuiGraphicsExtractor\\b", "GuiGraphics")
    }
}

private fun StonecutterBuildProperties.rendering() {
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

private fun StonecutterBuildProperties.renderingMethods() {
    linkedMapOf(
        "VertexFormat.Mode" to "com.mojang.blaze3d.PrimitiveTopology",
        "VertexFormatElement.COLOR" to "DefaultVertexFormat.COLOR_SEMANTIC_NAME",
        ".getVertexFormat()" to ".getVertexFormatBinding(0)",
        ".getGameRenderState()" to ".gameRenderState()",
        ".getMainRenderTarget()" to ".gameRenderer.mainRenderTarget()",
        "GpuBuffer.MappedView" to "com.mojang.blaze3d.buffers.GpuBufferSlice.MappedView"
    ).forEach { (old, new) -> rename("26.2", old, new) }
}

private fun StonecutterBuildProperties.clientAnnotations() {
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
