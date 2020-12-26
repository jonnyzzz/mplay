package com.jonnyzzz.mplay.gradle

import org.gradle.api.Project


/**
 * Use the `mplay { ... }` extension in Gradle to
 * specify the classpath of the application and the
 * class names of the classes, where the event capturing
 * should be added.
 *
 * In exchange, the plugin would generate a subs for all
 * non-trivial places
 */
interface MPlayExtension


open class MPlayExtensionImpl(
    val project: Project
) : MPlayExtension


