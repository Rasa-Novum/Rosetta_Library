import org.gradle.language.jvm.tasks.ProcessResources

val packFormatFields = extra["rosetta.packFormatFields"].toString()
tasks.named<ProcessResources>("processResources") {
    inputs.property("rosettaPackFormatFields", packFormatFields)
    filesMatching("pack.mcmeta") {
        expand("pack_format_fields" to packFormatFields)
    }
}
