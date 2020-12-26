package com.jonnyzzz.mplay.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.jonnyzzz.mplay.gradle.generated.MPlayVersions
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import java.io.File

private const val mplayVersion = MPlayVersions.buildNumber

@Suppress("unused")
class MPlayPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = MPlayExtensionImpl(project)
        project.extensions.add(MPlayExtension::class.java, "mplay", ext)

        project.plugins.apply("java")

        val builderConfiguration = project.configurations.create("mplay-agent-builder") {
            it.isVisible = false
        }


        project.dependencies.apply {
            add(
                "implementation",
                module("com.jonnyzzz.mplay:config:$mplayVersion")
            )
            add(
                builderConfiguration.name,
                module("com.jonnyzzz.mplay:agent-builder:$mplayVersion")
            )
        }

        val agentOutput = File(project.buildDir, "mplay-agent/mplay-${project.name}-agent.jar")
        val buildAgentTask = project.tasks.create("mplayBuildJavaagent") {
            it.group = "mplay"
        }

        val buildAgentTaskImpl = project.tasks.create("mplayBuildJavaagentRun", JavaExec::class.java) {
            it.group = "mplay"
            it.mainClass.set("com.jonnyzzz.mplay.agent.builder.BuilderMain") //TODO = use manifest

            it.doFirst {
                it as JavaExec
                it.classpath += builderConfiguration
                it.args(
                    "--classpath=" + project.resolveMainClasspath().joinToString(File.pathSeparator),
                    "--output=$agentOutput"
                )
            }
        }

        buildAgentTask.dependsOn(buildAgentTaskImpl)
    }

    private fun Project.resolveMainClasspath(): FileCollection {
        val sourceSets = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets
        val main = sourceSets.getByName(MAIN_SOURCE_SET_NAME)
        return main.runtimeClasspath
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

