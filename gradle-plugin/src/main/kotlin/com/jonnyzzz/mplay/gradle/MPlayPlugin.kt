package com.jonnyzzz.mplay.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

private const val mplayVersion = "SNAPSHOT"

@Suppress("unused")
class MPlayPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = MPlayExtensionImpl(project)
        project.extensions.add(MPlayExtension::class.java, "mplay", ext)

        project.plugins.apply("java")
        project.dependencies.apply {
            add(
                "implementation",
                module("com.jonnyzzz.mplay:config:$mplayVersion")
            )
        }

        val generateStubsTask = project.tasks.create("mplayGenerateStubsTask")
        val buildAgentTask = project.tasks.create("mplayBuildJavaagent")
    }
}


/**
 * Use the `mplay { ... }` extension in Gradle to
 * specify the classpath of the application and the
 * class names of the classes, where the event capturing
 * should be added.
 *
 * In exchange, the plugin would generate a subs for all
 * non-trivial places
 */
interface MPlayExtension {

}

open class MPlayExtensionImpl(
    val project: Project
) : MPlayExtension {

}

