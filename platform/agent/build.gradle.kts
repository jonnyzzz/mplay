@file:Suppress("HasPlatformType")

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    api("org.ow2.asm:asm:9.0")
    api("org.ow2.asm:asm-commons:9.0")
    api("org.ow2.asm:asm-util:9.0")

    implementation(project(":agent-config"))
    implementation(project(":agent-runtime"))
    implementation(kotlin("stdlib-jdk8"))

    testImplementation("junit:junit:4.12")
}

tasks.shadowJar.configure {
    exclude("**module-info.class",
        "META-INF/INDEX.LIST",
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
        "META-INF/proguard/**",
        "META-INF/com.android.tools/**",
        "META-INF/maven/**")

    fun relocate(pkg: String) = relocate(pkg, "com.jonnyzzz.mplay.shadow.$pkg")
    fun relocateAll(vararg pkg: String) = pkg.forEach { relocate(it) }
    relocateAll(
        "kotlin",
        "net.bytebuddy",
        "org.intellij",
        "org.jetbrains",
        "org.objectweb.asm",
        "com.fasterxml.jackson"
    )
}

val bundle by configurations.creating
val bundleJar by tasks.creating(Jar::class.java) {
    dependsOn(tasks.shadowJar)
    dependsOn(tasks.classes)

    archiveBaseName.set("mplay-agent-bundle")

    from({ zipTree(tasks.shadowJar.map { it.archiveFile }.get())})

    manifest {
        attributes(
            "Premain-Class" to "com.jonnyzzz.mplay.agent.MPlayAgent",
            "Can-Redefine-Classes" to false,
            "Can-Set-Native-Method-Prefix" to false
        )
    }
}

artifacts.add(bundle.name, bundleJar.archiveFile) {
    builtBy(bundleJar)
}

val versionRoot = File(buildDir, "version")

sourceSets {
    getByName("main") {
        java {
            srcDir(versionRoot)
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
            package com.jonnyzzz.mplay.agent.generated

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
