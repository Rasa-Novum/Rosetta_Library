import org.gradle.language.jvm.tasks.ProcessResources

val minecraftVersion = project.name.substringBeforeLast('-')
val packFormat = when (minecraftVersion) {
    "1.20.1" -> 15
    "1.21.1" -> 34
    "26.1" -> 84
    "26.2" -> 88
    "26.3" -> 97
    else -> error("Unsupported resource-pack format for $minecraftVersion")
}

tasks.named<ProcessResources>("processResources") {
    val packFormatFields = if (minecraftVersion == "26.3") {
        "\"min_format\": [97, 1], \"max_format\": [97, 1]"
    } else if (minecraftVersion.startsWith("26.")) {
        "\"min_format\": $packFormat, \"max_format\": $packFormat"
    } else {
        "\"pack_format\": $packFormat"
    }
    inputs.property("rosettaPackFormatFields", packFormatFields)
    filesMatching("pack.mcmeta") {
        expand("pack_format_fields" to packFormatFields)
    }
}
