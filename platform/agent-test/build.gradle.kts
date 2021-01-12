plugins {
    kotlin("jvm")
}

val runtimeRecorder by configurations.creating
val runtimeAgent by configurations.creating

dependencies {
    testImplementation(project(":agent"))
    testImplementation(project(":agent-config"))
    testImplementation(project(":agent-builder"))
    testImplementation(project(":agent-runtime"))
    testImplementation(project(":config"))
    testImplementation("junit:junit:4.12")

    runtimeRecorder(project(":agent-runtime-recorder"))
    runtimeAgent(project(":agent", configuration = "bundle"))
}

val runtimeRecorderDir = File(buildDir, "recorder/files")

val runtimeRecorderClasspathFile = File(buildDir, "recorder/classpath.txt")

val runtimeRecorderFiles by tasks.creating(Sync::class.java) {
    destinationDir = runtimeRecorderDir
    from(runtimeRecorder)
}

val runtimeRecorderClasspath by tasks.creating {
    dependsOn(runtimeRecorder)
    dependsOn(runtimeRecorderFiles)
    outputs.file(runtimeRecorderClasspathFile)
    doFirst {
        //we resolve configuration to make sure the classpath order is the same
        val classpath = runtimeRecorder.resolve().map {
            require(it.isFile && it.name.endsWith(".jar")) {
                "Unexpected file: $it in ${project.path}"
            }
            it.canonicalFile
        }.distinct()
        runtimeRecorderClasspathFile.parentFile?.mkdirs()
        runtimeRecorderClasspathFile.writeText(classpath.joinToString("\n"))
    }
}

sourceSets {
    val smoke1 by sourceSets.creating
    val smoke1config by sourceSets.creating {
        compileClasspath += smoke1.output
        runtimeClasspath += smoke1.output
    }

    dependencies {
        smoke1config.implementationConfigurationName(project(":config"))
    }

    @Suppress("UNUSED_VARIABLE")
    val runSmoke1 by tasks.creating(JavaExec::class.java) {
        group = "verification"
        dependsOn(configurations.named(sourceSets["test"].runtimeClasspathConfigurationName))
        dependsOn(smoke1.classesTaskName)
        dependsOn(smoke1config.classesTaskName)
        dependsOn(runtimeAgent)
        dependsOn(runtimeRecorderClasspath)
        mainClass.set("com.jonnyzzz.mplay.agent.smoke1.Smoke1MainKt")
        doFirst {
            val fakeConfigFile = file("src/smoke1/agent-config.json")
            val agentJar = runtimeAgent.singleFile
            val recordDir = File(buildDir, "smoke1-record")
            delete(recordDir)

            val configClasspathFile = File(buildDir, "${smoke1config.name}-classpath.txt")
            configClasspathFile.writeText(smoke1config.runtimeClasspath.joinToString("\n"))

            classpath = smoke1.runtimeClasspath
            enableAssertions = true
            jvmArgs = listOf("-javaagent:$agentJar=" +
                    "config=$fakeConfigFile;" +
                    "recorder-classpath=$runtimeRecorderClasspathFile;" +
                    "record-dir=${recordDir};" +
                    "config-classpath=${configClasspathFile}"
//                "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
            )
        }
    }
}
