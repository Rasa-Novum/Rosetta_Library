package net.rasanovum.rosetta.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
/** Resolves explicitly supplied jars without changing their module coordinates. */
class RosettaLocalDependenciesPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (!project.providers.gradleProperty("localDependencies").map(String::toBoolean).getOrElse(true)) return
        val root = project.rootProject.file(project.providers.gradleProperty("localDependenciesDir").getOrElse("libs/local"))
        val target = project.providers.gradleProperty("localDependenciesTarget").getOrElse(project.name)
        require(target.isNotBlank() && target != "." && target != ".." && '/' !in target && '\\' !in target) {
            "Local dependency target must be a single directory name"
        }
        val directory = root.resolve(target)
        val jars = project.fileTree(directory).matching { include("*/*.jar") }.files.sortedBy { it.path }
        val modules = jars.map { it.parentFile.name to it.nameWithoutExtension }
        if (modules.isEmpty()) return
        val local = project.repositories.ivy {
            name = "rosettaLocalDependencies"
            url = directory.toURI()
            patternLayout {
                artifact("[organisation]/[artifact](-[classifier]).[ext]")
                ivy("[organisation]/[module].ivy.xml")
            }
            metadataSources {
                ivyDescriptor()
                artifact()
            }
            content { modules.forEach { (group, module) -> includeModule(group, module) } }
        }
        // Exclude only discovered overrides, including from repositories added later.
        project.repositories.configureEach {
            if (this != local) content {
                modules.forEach { (group, module) -> excludeModule(group, module) }
            }
        }
        modules.forEach { (group, module) ->
            project.logger.lifecycle("Local dependency override (${project.path}): $group:$module -> ${directory.resolve(group).resolve("$module.jar")}")
        }
    }
}
