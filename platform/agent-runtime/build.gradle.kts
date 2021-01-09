@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}

dependencies {
    implementation(project(":config"))
    implementation(project(":agent-config"))
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
