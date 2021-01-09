plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    api("net.bytebuddy:byte-buddy-dep:1.10.19")
    api("org.ow2.asm:asm:9.0")
    api("org.ow2.asm:asm-commons:9.0")
    api("org.ow2.asm:asm-util:9.0")

    implementation(project(":agent-config"))
    implementation(project(":agent-runtime"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    testImplementation("junit:junit:4.12")
}

sourceSets {
    val test by this.getting

    val smoke1 by sourceSets.creating {
        compileClasspath += test.compileClasspath
        runtimeClasspath += test.runtimeClasspath
    }

    @Suppress("UNUSED_VARIABLE")
    val runSmoke1 by tasks.creating(JavaExec::class.java) {
        group = "verification"
        dependsOn(tasks.shadowJar)
        mainClass.set("org.jonnyzzz.mplay.agent.smoke1.MainKt")
        doFirst {
            classpath = smoke1.runtimeClasspath
            enableAssertions = true
            jvmArgs = listOf("-javaagent:${tasks.shadowJar.get().archiveFile.get().asFile}")
        }
    }
}

tasks.shadowJar.configure {
    inputs.file(project.buildFile)

    exclude("**module-info.class", "META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

    manifest {
        attributes(
            "Premain-Class" to "com.jonnyzzz.mplay.agent.MPlayAgent",
            "Can-Redefine-Classes" to false,
            "Can-Set-Native-Method-Prefix" to false
        )
    }

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

    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}
