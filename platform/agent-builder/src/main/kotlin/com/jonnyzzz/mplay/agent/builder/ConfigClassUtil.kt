package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.agent.config.AgentConfig
import com.jonnyzzz.mplay.agent.config.InterceptClassTask
import com.jonnyzzz.mplay.agent.config.InterceptMethodTask
import com.jonnyzzz.mplay.config.MPlayConfiguration
import org.objectweb.asm.Type
import java.lang.reflect.Method


inline fun <reified T : MPlayConfiguration<*>> ConfigurationClass.Companion.fromConfigClass() =
    fromConfigClass(T::class.java)

inline fun <reified T> ConfigurationClass.Companion.fromClass(): ConfigurationClass {
    val x = object : MPlayConfiguration<T> {}
    return fromConfigClass(x.javaClass)
}

fun ConfigurationClass.toClasspath(): ConfigurationClasspath = ConfigurationClasspath(listOf(this))


fun ConfigurationClasspath.toAgentConfig(): AgentConfig {
    return AgentConfig(classesToRecordEvents = this.configurationClasses.map {
        InterceptClassTask(
            classNameToIntercept = it.interceptedRawType.name,
            configClassName = it.interceptedRawType.name,
            methodsToRecord = it.methodsToIntercept.map { m -> it.toAgentConfig(m) }
        )
    })
}

fun ConfigurationClass.toAgentConfig(m: Method): InterceptMethodTask {
    return InterceptMethodTask(
        methodName = m.name,
        jvmMethodDescriptor = Type.getMethodDescriptor(m)
    )
}
