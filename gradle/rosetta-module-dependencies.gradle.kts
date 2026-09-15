val module = project.parent!!.name
val dependenciesByModule = mapOf(
    "networking" to listOf("core"),
    "attachments" to listOf("core"),
    "resources" to listOf("core"),
    "resources-sync" to listOf("core", "networking", "resources"),
    "config" to listOf("networking"),
)
val legacyFabric = project.name.endsWith("-fabric") && !project.name.startsWith("26.")
dependenciesByModule.getValue(module).forEach { dependency ->
    val path = if (dependency == "core") ":${project.name}" else ":$dependency:${project.name}"
    dependencies.add("implementation", if (legacyFabric) {
        dependencies.project(mapOf("path" to path, "configuration" to "namedElements"))
    } else {
        dependencies.project(mapOf("path" to path))
    })
}
