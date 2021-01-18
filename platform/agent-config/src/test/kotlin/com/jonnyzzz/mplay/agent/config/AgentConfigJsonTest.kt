@file:Suppress("MemberVisibilityCanBePrivate")

package com.jonnyzzz.mplay.agent.config

import org.junit.Assert
import org.junit.Test

class AgentConfigJsonTest {
    val interceptTask = InterceptClassTask(
        classNameToIntercept = "a",
        configClassName = "b",
        methodsToRecord = listOf(InterceptMethodTask(MethodRef("a", "()V"), null)),
        constructorsToIntercept = listOf(InterceptConstructorTask(MethodRef.ctor("()V"))),
    )

    val openTask = OpenClassMethodsTask(
        "base",
        methodsToOpen = listOf(OpenMethodTask(MethodRef("c", "(I)V")))
    )

    val config = AgentConfig(
        configClasspath = listOf("cp"),
        classesToRecordEvents = listOf(interceptTask),
        classesToOpenMethods = listOf(openTask)
    )

    @Test
    fun testLoadSafe() {
        val bytes = saveAgentConfig(config)
        val reload = loadAgentConfig(bytes)
        Assert.assertEquals(config, reload)
    }
}
