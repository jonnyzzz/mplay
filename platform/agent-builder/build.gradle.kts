
plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    `java-library`
    `maven-publish`
}

val mplayMainClassName = "com.jonnyzzz.mplay.agent.builder.BuilderMain"
val agentJar by configurations.creating

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":config"))
    implementation("org.ow2.asm:asm:9.0")

    agentJar(project(path = ":agent", configuration = "shadow"))
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven-shadow") {
            shadow.component(this)
            artifact(tasks.kotlinSourcesJar)
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

tasks.shadowJar.configure {
    manifest {
        attributes("Main-Class" to mplayMainClassName)
    }

    archiveClassifier.set("")

    relocate("net.bytebuddy", "com.jonnyzzz.mplay.shadow.net.bytebuddy")
    relocate("kotlin", "com.jonnyzzz.mplay.shadow.kotlin")
    relocate("org.intellij", "com.jonnyzzz.mplay.shadow.org.intellij")
    relocate("org.jetbrains", "com.jonnyzzz.mplay.shadow.org.jetbrains")
    minimize()

    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
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

