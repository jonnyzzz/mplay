plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation("net.bytebuddy:byte-buddy:1.10.18")
    testImplementation("junit:junit:4.12")
}

sourceSets {
    val test by this.getting

    val smoke1 by sourceSets.creating {
        compileClasspath += test.compileClasspath
        runtimeClasspath += test.runtimeClasspath
    }

    val shadowJar = tasks.getByName("shadowJar") as com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

    val runSmoke1 by tasks.creating(JavaExec::class.java) {
        dependsOn(shadowJar)
        mainClass.set("org.jonnyzzz.mplay.agent.smoke1.MainKt")
        doFirst {
            classpath = smoke1.runtimeClasspath
            enableAssertions = true
            jvmArgs = listOf("-javaagent:${shadowJar.archiveFile.get().asFile}")
        }
    }
}

tasks.shadowJar.configure {
    manifest {
        attributes("Premain-Class" to "org.jonnyzzz.mplay.agent.MPlayAgent")
    }
}

tasks.withType(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class.java) {
    relocate("net.bytebuddy", "org.jonnyzzz.mplay.shadow.net.bytebuddy")
    relocate("kotlin", "org.jonnyzzz.mplay.shadow.kotlin")
    relocate("org.intellij", "org.jonnyzzz.mplay.shadow.org.intellij")
    relocate("org.jetbrains", "org.jonnyzzz.mplay.shadow.org.jetbrains")
    minimize()
}

tasks.withType(AbstractArchiveTask::class.java) {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}
