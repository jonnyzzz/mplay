plugins {
    kotlin("jvm") version "1.4.20" apply false
    kotlin("plugin.serialization") version "1.4.20" apply false
    id("com.github.johnrengelman.shadow") version "6.1.0" apply false
}

apply(from = File(rootProject.projectDir, "../common.gradle.kts"))

val publishToMavenLocalForExamples by tasks.creating {
    dependsOn(":config:publishToMavenLocal")
    dependsOn(":agent-builder:publishToMavenLocal")
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}
