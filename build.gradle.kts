
plugins {
    id("java")
}

val MINECRAFT_VERSION by extra { "1.20.4" }
val NEOFORGE_VERSION by extra { "20.4.219" }
val FABRIC_LOADER_VERSION by extra { "0.15.6" }
val FABRIC_API_VERSION by extra { "0.96.0+1.20.4" }

// https://semver.org/
val MOD_VERSION by extra { "2.0.0" }

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
}
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

subprojects {
    repositories {
        maven(url = "https://maven.flashyreese.me/releases")
        maven(url = "https://maven.flashyreese.me/snapshots")
    }

    apply(plugin = "maven-publish")

    java.toolchain.languageVersion = JavaLanguageVersion.of(17)


    fun createVersionString(): String {
        val builder = StringBuilder()

        val isReleaseBuild = project.hasProperty("build.release")
        val buildId = System.getenv("GITHUB_RUN_NUMBER")

        if (isReleaseBuild) {
            builder.append(MOD_VERSION)
        } else {
            builder.append(MOD_VERSION.substringBefore('-'))
            builder.append("-snapshot")
        }

        builder.append("+mc").append(MINECRAFT_VERSION)

        if (!isReleaseBuild) {
            if (buildId != null) {
                builder.append("-build.${buildId}")
            } else {
                builder.append("-local")
            }
        }

        return builder.toString()
    }

    tasks.processResources {
        filesMatching("META-INF/mods.toml") {
            expand(mapOf("version" to createVersionString()))
        }
    }

    version = createVersionString()
    group = "me.flashyreese.mods"

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    // Disables Gradle's custom module metadata from being published to maven. The
    // metadata includes mapped dependencies which are not reasonably consumable by
    // other mod developers.
    tasks.withType<GenerateModuleMetadata>().configureEach {
        enabled = false
    }
}