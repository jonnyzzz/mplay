package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.agent.config.*
import com.jonnyzzz.mplay.config.MPlayConfiguration
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.nio.file.Path


inline fun <reified T : MPlayConfiguration<*>> ConfigurationClass.Companion.fromConfigClass() =
    fromConfigClass(T::class.java)

inline fun <reified T> ConfigurationClass.Companion.fromClass(): ConfigurationClass {
    val x = object : MPlayConfiguration<T> {}
    return fromConfigClass(x.javaClass)
}

fun ConfigurationClass.toClasspath(): ConfigurationClasspath = ConfigurationClasspath(listOf(this))


fun ConfigurationClasspath.toAgentConfig(classpath: List<Path> = listOf()): AgentConfig {
    val methodsToImplement = this.configurationClasses
        .flatMap { clazz ->
            clazz.methodsToIntercept.mapNotNull {
                clazz.toImplementMethodTask(it)
            }
        }
        .groupBy({ (clazz, _) -> clazz.name }, { (_, task) -> task })
        .map { (clazz, methods) ->
            OpenClassMethodsTask(clazz, methods)
        }

    val methodsToRecord = this.configurationClasses.map { clazz ->
        InterceptClassTask(
            classNameToIntercept = clazz.interceptedRawType.name,
            configClassName = clazz.interceptedRawType.name,
            methodsToRecord = clazz.methodsToIntercept.map { clazz.toInterceptMethodTask(it) },
            constructorsToIntercept = clazz.constructorsToIntercept.map { InterceptConstructorTask(it.toMethodRef()) }
        )
    }

    return AgentConfig(
        configClasspath = classpath.map { it.toAbsolutePath().toString() },
        classesToOpenMethods = methodsToImplement,
        classesToRecordEvents = methodsToRecord,
    )
}

fun ConfigurationClass.toInterceptMethodTask(m: Method): InterceptMethodTask {
    val declaredClazz = m.declaringClass

    return InterceptMethodTask(
        methodRef = m.toMethodRef(),
        defaultMethodOfInterface = declaredClazz.takeIf { it.isInterface }?.name
    )
}

fun ConfigurationClass.toImplementMethodTask(m: Method): Pair<Class<*>, OpenMethodTask>? {
    val declaringType = m.declaringClass
    if (declaringType == interceptedRawType) return null
    if (declaringType.isInterface) return null
    if (!Modifier.isFinal(m.modifiers)) return null

    return declaringType to OpenMethodTask(
        methodRef = m.toMethodRef()
    )
}
