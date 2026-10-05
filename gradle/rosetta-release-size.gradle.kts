import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.compile.JavaCompile
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

val debugLocals = providers.gradleProperty("rosettaDebugLocals").map(String::toBoolean).orElse(false)
tasks.withType<JavaCompile>().configureEach {
    options.debugOptions.debugLevel = if (debugLocals.get()) "source,lines,vars" else "source,lines"
}

val loader = project.name.substringAfterLast('-')
val minecraftVersion = project.name.substringBeforeLast('-')
val releaseTask = when {
    loader == "fabric" && minecraftVersion in setOf("1.20.1", "1.21.1") -> "remapJar"
    loader == "forge" -> "reobfJar"
    else -> "jar"
}

tasks.named<AbstractArchiveTask>(releaseTask) {
    doLast {
        val archive = archiveFile.get().asFile
        val temporary = archive.resolveSibling("${archive.name}.tmp")
        try {
            JarFile(archive).use { input ->
                val manifest = (input.manifest ?: Manifest()).apply {
                    if (loader == "fabric") mainAttributes.remove(Attributes.Name("Fabric-Loom-Version"))
                }
                JarOutputStream(temporary.outputStream()).use { output ->
                    output.setLevel(9)
                    output.putNextEntry(JarEntry(JarFile.MANIFEST_NAME).apply {
                        time = input.getJarEntry(JarFile.MANIFEST_NAME)?.time ?: 0L
                    })
                    manifest.write(output)
                    output.closeEntry()
                    input.entries().asSequence()
                        .filterNot { it.name == JarFile.MANIFEST_NAME || it.isDirectory }
                        .forEach { entry ->
                            output.putNextEntry(JarEntry(entry.name).apply { time = entry.time })
                            input.getInputStream(entry).use { it.copyTo(output) }
                            output.closeEntry()
                        }
                }
            }
            temporary.copyTo(archive, overwrite = true)
        } finally {
            temporary.delete()
        }
    }
}
