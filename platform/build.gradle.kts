plugins {
    kotlin("jvm") version "1.4.20" apply false
}

apply(from = File(rootProject.projectDir, "../common.gradle.kts"))

val publishToMavenLocalForExamples by tasks.creating {
    dependsOn(":config:publishToMavenLocal")
}
