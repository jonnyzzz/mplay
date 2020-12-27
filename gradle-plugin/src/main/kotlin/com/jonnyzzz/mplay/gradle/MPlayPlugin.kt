package com.jonnyzzz.mplay.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.jonnyzzz.mplay.gradle.generated.MPlayVersions
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.Sync
import java.io.File
import java.util.concurrent.Callable

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
                module(mapOf(
                    "group" to "com.jonnyzzz.mplay",
                    "name" to "agent-builder",
                    "version" to mplayVersion,
                    "ext" to "zip"
                ))
            )
        }

        val agentOutput = File(project.buildDir, "mplay/agent/mplay-${project.name}-agent.jar")
        val buildAgentTask = project.tasks.create("mplayBuildJavaagent") {
            it.group = "mplay"
        }

        val buildAgentBuilderTaskUnpack = project.tasks.create("mplayBuildJavaagentPrepare", Sync::class.java) { task ->
            task.run {
                inputs.property("version", mplayVersion)
                group = "mplay"
                from( Callable { project.zipTree(builderConfiguration.singleFile) } ) {
                    into("")
                }
                destinationDir = File(project.buildDir, "mplay/agent-builder")
            }
        }

        val buildAgentTaskImpl = project.tasks.create("mplayBuildJavaagentRun", JavaExec::class.java) { task ->
            task.run {
                group = "mplay"
                dependsOn(buildAgentBuilderTaskUnpack)
                dependsOn(project.tasks.named("classes"))

                inputs.property("version", mplayVersion)
                mainClass.set("com.jonnyzzz.mplay.agent.builder.BuilderMain")
                val classpathFile = File(project.buildDir, "mplay/generate-classpath.txt")

                doFirst {
                    classpathFile.parentFile?.mkdirs()
                    classpathFile.writeText(project.resolveMainClasspath().joinToString("\n"))

                    classpath += project.fileTree(buildAgentBuilderTaskUnpack.destinationDir) {
                        it.include("**/lib/*.jar")
                    }

                    args(
                        "--classpathFile=$classpathFile",
                        "--output=$agentOutput"
                    )
                }
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

