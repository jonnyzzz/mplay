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
    manifest {
        attributes(
            "Premain-Class" to "com.jonnyzzz.mplay.agent.MPlayAgent",
            "Can-Redefine-Classes" to false,
            "Can-Set-Native-Method-Prefix" to false
        )
    }

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

sourceSets {
    val smoke1 by sourceSets.creating

    @Suppress("UNUSED_VARIABLE")
    val runSmoke1 by tasks.creating(JavaExec::class.java) {
        group = "verification"
        dependsOn(tasks.shadowJar)
        dependsOn(tasks.classes)
        dependsOn(smoke1.classesTaskName)
        mainClass.set("com.jonnyzzz.mplay.agent.smoke1.Smoke1MainKt")
        doFirst {
            val fakeConfigFile = file("src/smoke1/agent-config.json")
            classpath = smoke1.runtimeClasspath
            enableAssertions = true
            jvmArgs = listOf("-javaagent:${tasks.shadowJar.get().archiveFile.get().asFile}=config=${fakeConfigFile.canonicalFile}")
        }
    }
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
