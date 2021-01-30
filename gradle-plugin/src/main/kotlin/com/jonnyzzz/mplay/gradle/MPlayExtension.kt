package com.jonnyzzz.mplay.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Property
import java.io.File


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
    val agentTask : MPlayAgentTask
    val recorderDir : Property<File>
}

interface MPlayAgentTask : Task {
    val mplayAgentArgs : Property<String>
    val mplayAgentPath : Property<File>
}

open class MPlayExtensionImpl(
    override val agentTask : MPlayAgentTask,
    override val recorderDir : Property<File>
) : MPlayExtension


