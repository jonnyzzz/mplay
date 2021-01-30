package com.jonnyzzz.mplay.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.jonnyzzz.mplay.gradle.generated.MPlayVersions
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.provider.Property
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.Sync
import java.io.File
import java.util.concurrent.Callable
import javax.inject.Inject

private const val mplayVersion = MPlayVersions.buildNumber

@Suppress("unused")
class MPlayPlugin : Plugin<Project> {
    override fun apply(project: Project) {

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
                module(
                    mapOf(
                        "group" to "com.jonnyzzz.mplay",
                        "name" to "agent-builder",
                        "version" to mplayVersion,
                        "ext" to "zip"
                    )
                )
            )
        }

        val recorderDir = project.objects.property(File::class.java)
        recorderDir.set(File(project.buildDir, "mplay-record"))

        val buildAgentTask = project.tasks.create("mplayBuildJavaagent", MPlayBuildAgentTask::class.java, project)

        val buildAgentBuilderTaskUnpack = project.tasks.create("mplayBuildJavaagentPrepare", Sync::class.java) { task ->
            task.run {
                inputs.property("version", mplayVersion)
                group = "mplay"
                from(Callable { project.zipTree(builderConfiguration.singleFile) }) {
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

                    val agentJar = project.fileTree(buildAgentBuilderTaskUnpack.destinationDir) {
                        it.include("**/agent/*.jar")
                    }.singleFile

                    val agentRuntime = project.fileTree(buildAgentBuilderTaskUnpack.destinationDir) {
                        it.include("**/agent-runtime/*.jar")
                    }

                    @Suppress("UnstableApiUsage")
                    recorderDir.finalizeValue()

                    args(
                        listOf(
                            "--classpathFile=$classpathFile",
                            "--agent-path=${buildAgentTask.agentOutput}",
                            "--agent-args-file=${buildAgentTask.agentArgs}",
                            "--agent-jar=${agentJar}",
                            "--agent-config=${buildAgentTask.agentConfig}",
                            "--record-dir=${recorderDir.get()}"
                        ) + agentRuntime.files.map { "--agent-runtime=${it}" }
                    )
                }
                doLast {
                    buildAgentTask.mplayAgentArgs.set(buildAgentTask.agentArgs.readText())
                }
            }
        }

        buildAgentTask.dependsOn(buildAgentTaskImpl)

        val ext = MPlayExtensionImpl(buildAgentTask, recorderDir)
        project.extensions.add(MPlayExtension::class.java, "mplay", ext)
    }

    private fun Project.resolveMainClasspath(): FileCollection {
        val sourceSets = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets
        val main = sourceSets.getByName(MAIN_SOURCE_SET_NAME)
        return main.runtimeClasspath
    }
}

fun Task.mPlayGroup() {
    group = "mplay"
}

open class MPlayBuildAgentTask
@Inject
constructor(project: Project) : DefaultTask(), MPlayAgentTask {
    val agentArgs = File(project.buildDir, "mplay/agent/mplay-${project.name}-agent.args.txt")
    val agentConfig = File(project.buildDir, "mplay/agent/mplay-${project.name}-agent.config.json")
    val agentOutput = File(project.buildDir, "mplay/agent/mplay-${project.name}-agent.jar")

    override val mplayAgentPath: Property<File> = project.objects.property(File::class.java).also {
        it.set(agentOutput)
    }

    override val mplayAgentArgs: Property<String> = project.objects.property(String::class.java)

    init {
        mPlayGroup()
    }
}
