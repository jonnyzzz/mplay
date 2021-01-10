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
