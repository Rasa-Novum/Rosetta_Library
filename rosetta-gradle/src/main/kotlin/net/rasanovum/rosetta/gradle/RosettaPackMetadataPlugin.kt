package net.rasanovum.rosetta.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class RosettaPackMetadataPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.extraProperties["rosetta.packFormatFields"] = PackMetadata.formatFields(
            project.name.substringBeforeLast('-'), project.name.substringAfterLast('-'))
    }
}
