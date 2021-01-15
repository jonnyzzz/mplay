@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}

dependencies {
    compileOnly(project(":config"))
    compileOnly(project(":agent-runtime"))
    compileOnly(project(":agent-config"))

    implementation("com.fasterxml.jackson.core:jackson-core:2.12.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.0")
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
