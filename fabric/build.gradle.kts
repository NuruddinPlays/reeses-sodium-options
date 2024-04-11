plugins {
    java
    idea
    `maven-publish`
    id("fabric-loom") version("1.6.5")
}

val MINECRAFT_VERSION: String by rootProject.extra
val FABRIC_LOADER_VERSION: String by rootProject.extra
val FABRIC_API_VERSION: String by rootProject.extra
val MOD_VERSION: String by rootProject.extra


base {
    archivesName.set("reeses-sodium-options-fabric-${MINECRAFT_VERSION}")
}

dependencies {
    minecraft("com.mojang:minecraft:${MINECRAFT_VERSION}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")

    modImplementation(group = "net.caffeinemc.mods", name = "common", version = "0.6.0-snapshot+mc1.20.4-local")
    modImplementation(group = "net.caffeinemc.mods", name = "sodium-fabric-1.20.4", version = "0.6.0-snapshot+mc1.20.4-local")
    compileOnly(project(":common"))
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.96.11+1.20.4")
}

loom {
    @Suppress("UnstableApiUsage")
    mixin { defaultRefmapName.set("sodium.refmap.json") }

    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("run")
        }
        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir("run")
        }
    }
}

tasks {
    withType<JavaCompile> {
        source(project(":common").sourceSets.main.get().allSource)
    }

    javadoc { source(project(":common").sourceSets.main.get().allJava) }

    processResources {
        from(project(":common").sourceSets.main.get().resources) {
            exclude("sodium.accesswidener")
        }

        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
    }
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "FlashyReeseReleases"
            url = uri("https://maven.flashyreese.me/releases")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
        maven {
            name = "FlashyReeseSnapshots"
            url = uri("https://maven.flashyreese.me/snapshots")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
        maven("file://${System.getenv("local_maven")}")
    }
}