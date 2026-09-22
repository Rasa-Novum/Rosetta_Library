repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "vendoredMidnightLib"
                url = rootProject.uri("gradle/overrides")
            }
        }
        filter {
            includeVersion("eu.midnightdust", "midnightlib-forge", "1.9.3.1+1.18.2-backport.1")
            includeVersion("eu.midnightdust", "midnightlib-forge", "1.9.3.1+1.19.2-backport.1")
            includeVersion("maven.modrinth", "midnightlib", "1.9.3+26.2-neoforge")
            includeVersion("maven.modrinth", "midnightlib", "1.9.3+26.3-neoforge")
        }
    }
}
