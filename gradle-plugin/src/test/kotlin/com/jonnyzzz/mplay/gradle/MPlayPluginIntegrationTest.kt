package com.jonnyzzz.mplay.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File


const val mplayPluginId = "com.jonnyzzz.mplay"

class GradleIntegrationTest {
    @get:Rule
    val testProjectDir = TemporaryFolder()

    private lateinit var settingsFile: File
    private lateinit var buildFile: File

    @Before
    fun setup() {
        settingsFile = testProjectDir.newFile("settings.gradle")
        buildFile = testProjectDir.newFile("build.gradle")
    }

    @Test
    fun sampleTest() {
        buildFile.writeText("plugins { id '$mplayPluginId' } ")

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("tasks", "--all")
            .withPluginClasspath()
            .build()

        println(result.output)
    }
}
