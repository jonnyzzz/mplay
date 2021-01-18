@file:Suppress("KDocUnresolvedReference")

package com.jonnyzzz.mplay.agent.config

import kotlinx.serialization.Serializable

/**
 * TODO: How should we deal with config classpath here?
 * Is it fine to assume these classes are included into the agent? Or mayby in the app classpath?
 */
@Serializable
data class AgentConfig(
    /**
     * classpath of the configuration classes
     */
    val configClasspath: List<String>,

    val classesToRecordEvents: List<InterceptClassTask>,

    val classesToOpenMethods: List<OpenClassMethodsTask> = listOf(),
)

@Serializable
data class OpenClassMethodsTask(
    /**
     * Fully Qualified name of the class to record method calls
     */
    val classNameToIntercept: String,

    val methodsToOpen: List<OpenMethodTask>
)

@Serializable
data class InterceptClassTask(
    /**
     * Fully Qualified name of the class to record method calls
     */
    val classNameToIntercept: String,

    /**
     * Fully Qualified of the configuration class that implements
     * [MPlayConfiguration] with the [classNameToIntercept] as the
     * generic parameter
     */
    val configClassName: String? = null,

    /**
     * Specifies methods (and metadata) for which the recording is done
     */
    val methodsToRecord : List<InterceptMethodTask>,

    /**
     * Specifies constructors that should be used to inject
     * the initialization of the methods recorder.
     *
     * For example the constructors that delegates to another
     * constructors of the same type are likely to be excluded
     */
    val constructorsToIntercept: List<InterceptConstructorTask>,
)

@Serializable
data class MethodRef(
    val methodName: String,
    val descriptor: String,
) {
    override fun toString() = "$methodName $descriptor"
    companion object
}

fun MethodRef.Companion.ctor(descriptor: String) = MethodRef("<init>", descriptor)

@Serializable
data class InterceptConstructorTask(
    val methodRef: MethodRef,
)

@Serializable
data class InterceptMethodTask(
    val methodRef: MethodRef,

    /**
     * Specifies the interface who's default
     * method is included for the recording.
     * Uses the fully qualified name format
     */
    val defaultMethodOfInterface: String? = null,
)

@Serializable
data class OpenMethodTask(
    val methodRef: MethodRef,
)
