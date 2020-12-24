package com.jonnyzzz.mplay.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class MPlayPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = MPlayExtensionImpl(project)
        project.extensions.add(MPlayExtension::class.java, "mplay", ext)
    }
}


interface MPlayExtension {

}

open class MPlayExtensionImpl(
    val project: Project
) : MPlayExtension {

}

