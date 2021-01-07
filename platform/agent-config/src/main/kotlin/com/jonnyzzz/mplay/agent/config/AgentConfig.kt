@file:Suppress("KDocUnresolvedReference")

package com.jonnyzzz.mplay.agent.config

/**
 * TODO: How should we deal with config classpath here?
 * Is it fine to assume these classes are included into the agent? Or mayby in the app classpath?
 */
data class AgentConfig(
    /**
     * classpath of the configuration classes
     */
    val configClasspath: List<String>,

    val classesToRecordEvents: List<InterceptClassTask>,

    val classesToOpenMethods: List<OpenClassMethodsTask>,
)

data class OpenClassMethodsTask(
    /**
     * Fully Qualified name of the class to record method calls
     */
    val classNameToIntercept: String,

    val methodsToOpen: List<ImplementMethodTask>
)

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
    val configClassName: String,

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

data class MethodRef(
    val methodName: String,
    val jvmMethodDescriptor: String,
) {
    override fun toString() = "$methodName $jvmMethodDescriptor"
}

data class InterceptConstructorTask(
    val methodRef: MethodRef,
)

data class InterceptMethodTask(
    val methodRef: MethodRef,

    /**
     * Specifies the interface who's default
     * method is included for the recording.
     * Uses the fully qualified name format
     */
    val defaultMethodOfInterface: String?
)

data class ImplementMethodTask(
    val methodRef: MethodRef,
)
