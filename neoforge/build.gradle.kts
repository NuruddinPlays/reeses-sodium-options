plugins {
    id("idea")
    id("maven-publish")
    id("net.neoforged.gradle.userdev") version "7.0.81"
    id("java-library")
}
base {
    archivesName = "reeses-sodium-options-neoforge-1.20.4"
}

val MINECRAFT_VERSION: String by rootProject.extra
val MOD_VERSION: String by rootProject.extra

runs {
    configureEach {
        modSource(project.sourceSets.main.get())
    }
    create("client") {
        dependencies {
            runtime("net.caffeinemc.mods:sodium-common-1.20.4:0.6.0-snapshot+mc1.20.4-local")
        }
    }

    create("data") {
        programArguments.addAll("--mod", "reeses-sodium-options", "--all", "--output", file("src/generated/resources/").getAbsolutePath(), "--existing", file("src/main/resources/").getAbsolutePath())
    }
}

dependencies {
    implementation("net.neoforged:neoforge:20.4.219")
    compileOnly(project(":common"))
    implementation(group = "net.caffeinemc.mods", name = "sodium-common-1.20.4", version = "0.6.0-snapshot+mc1.20.4-local")
    //implementation("net.caffeinemc.mods:sodium-neoforge-1.20.4:0.6.0-snapshot+mc1.20.4-local")
}

// NeoGradle compiles the game, but we don't want to add our common code to the game's code
val notNeoTask: (Task) -> Boolean = { it: Task -> !it.name.startsWith("neo") && !it.name.startsWith("compileService") }

tasks.withType<JavaCompile>().matching(notNeoTask).configureEach {
    source(project(":common").sourceSets.main.get().allSource)
}

tasks.withType<Javadoc>().matching(notNeoTask).configureEach {
    source(project(":common").sourceSets.main.get().allJava)
}

tasks.withType<ProcessResources>().matching(notNeoTask).configureEach {
    from(project(":common").sourceSets.main.get().resources)
}

java.toolchain.languageVersion = JavaLanguageVersion.of(17)

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
        maven {
            url = uri("file://" + System.getenv("local_maven"))
        }
    }
}