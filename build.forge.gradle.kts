plugins { id("net.rasanovum.rosetta.pack-metadata"); id("net.neoforged.moddev.legacyforge") }

apply(from = rootProject.file("gradle/rosetta-common.gradle.kts"))
fun prop(name: String): String = property(name).toString()

legacyForge {
    version = prop("deps.forge")
    if (prop("deps.minecraft") in setOf("1.18.2", "1.19.2")) {
        accessTransformers.from(file("accesstransformer.cfg"))
    }
    mods { register(prop("mod_id")) { sourceSet(sourceSets.main.get()) } }
}

apply(from = rootProject.file("gradle/rosetta-publishing.gradle.kts"))
apply(from = rootProject.file("gradle/rosetta-pack-metadata.gradle.kts"))

apply(from = rootProject.file("gradle/rosetta-release-size.gradle.kts"))

val legacy = prop("deps.minecraft") in setOf("1.18.2", "1.19.2")
val mixinConfigs = buildList {
    add("rosetta-common.mixins.json")
    if (legacy) add("rosetta-client.mixins.json")
    if (prop("deps.minecraft") == "1.18.2") add("rosetta-legacy.mixins.json")
}
mixin {
    add(sourceSets.main.get(), "rosetta.refmap.json")
    mixinConfigs.forEach { config(it) }
}
dependencies { annotationProcessor("org.spongepowered:mixin:0.8.7:processor") }
tasks.jar {
    manifest.attributes["MixinConfigs"] = mixinConfigs.joinToString(",")
    if (legacy) from("accesstransformer.cfg") { into("META-INF") }
}
