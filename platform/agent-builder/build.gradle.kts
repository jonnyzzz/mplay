@file:Suppress("UnstableApiUsage", "HasPlatformType")

plugins {
    kotlin("jvm")
    application
    `maven-publish`
}

application {
    mainClass.set("com.jonnyzzz.mplay.agent.builder.BuilderMain")
}

tasks.run.configure {
    args("--classpathFile=${rootProject.projectDir}/../example/how-to-use-the-plugin/the-mplay/build/mplay/generate-classpath.txt")
}

val agentJar by configurations.creating

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation(project(":config"))

    implementation("org.ow2.asm:asm:9.0")
    implementation("org.ow2.asm:asm-commons:9.0")
    implementation("org.ow2.asm:asm-util:9.0")

    implementation("com.squareup:kotlinpoet:1.7.2")
    implementation("org.reflections:reflections:0.9.12")

    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation(project(":agent-config"))
    implementation(project(":agent-runtime"))

    testImplementation("junit:junit:4.12")

    agentJar(project(path = ":agent", configuration = "bundle"))
}

java {
    withSourcesJar()
}

val agentBuilder by configurations.creating

val agentBuilderArtifact = artifacts.add(agentBuilder.name, tasks.distZip) {
    type = "app"
    builtBy(tasks.distZip)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(agentBuilderArtifact) {
//                extension = "jar"
//                classifier = "app"
            }
        }
    }
}

val agentJarRoot = File(buildDir, "agentJar")

val copyMPlayAgent by tasks.creating(Copy::class.java) {
    from(agentJar.singleFile) {
        rename { "mplay-agent.jar" }
        into("mplay-agent")
    }
    destinationDir = agentJarRoot
}

tasks.compileKotlin.configure {
    dependsOn(copyMPlayAgent)
}

val versionRoot = File(buildDir, "version")

sourceSets {
    getByName("main") {
        java {
            srcDir(versionRoot)
            resources.srcDir(agentJarRoot)
        }
    }
}

val generateVersion by tasks.creating {
    val versionFile = File(versionRoot, "version.kt")
    inputs.property("version", version)
    outputs.file(versionFile)

    doFirst {
        versionFile.parentFile?.mkdirs()
        versionFile.writeText("""
            package com.jonnyzzz.mplay.agent.builder.generated

            //!!!!!!!!!!!!
            //!!!!!!!!!!!!  NOTE: THIS FILES IS GENERATED !!!!!!!!!!!!!!!
            //!!!!!!!!!!!!
            
            object MPlayVersions {
                const val buildNumber = "$version" 
            }
            
        """.trimIndent())
    }
}

tasks.compileKotlin.configure {
    dependsOn(generateVersion)
}

