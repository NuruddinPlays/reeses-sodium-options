import net.fabricmc.loom.task.AbstractRemapJarTask

plugins {
    id("java")
    id("idea")
    id("fabric-loom") version "1.6.5"
}

val MINECRAFT_VERSION: String by rootProject.extra
val FABRIC_LOADER_VERSION: String by rootProject.extra
val FABRIC_API_VERSION: String by rootProject.extra

dependencies {
    "minecraft"(group = "com.mojang", name = "minecraft", version = MINECRAFT_VERSION)
    "mappings"(loom.officialMojangMappings())
    compileOnly("io.github.llamalad7:mixinextras-common:0.3.5")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.3.5")
    compileOnly("net.fabricmc:sponge-mixin:0.13.2+mixin.0.8.5")

    implementation(group = "net.caffeinemc.mods", name = "sodium-common-1.20.4", version = "0.6.0-snapshot+mc1.20.4-local")
    modImplementation(group = "net.caffeinemc.mods", name = "sodium-common-1.20.4", version = "0.6.0-snapshot+mc1.20.4-local")
    //modImplementation(group = "net.caffeinemc.mods", name = "sodium", version = "0.6.0-snapshot+mc1.20.4-local")
}

tasks.withType<AbstractRemapJarTask>().forEach {
    it.targetNamespace = "named"
}


loom {
    mixin {
        defaultRefmapName = "reeses-sodium-options.refmap.json"
    }

    mods {
        val main by creating { // to match the default mod generated for Forge
            sourceSet("main")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
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
        maven {
            url = uri("file://" + System.getenv("local_maven"))
        }
    }
}