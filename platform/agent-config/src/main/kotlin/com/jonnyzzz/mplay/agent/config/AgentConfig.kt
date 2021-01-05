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

    //we also need the set of classes, where methods would be made non-final
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
     * Specifies the list of methods from base classes which we need
     * to open in the base classes and implement in the current the
     * [classNameToIntercept] in order to inject methods recording
     */
    val methodsToImplement: List<ImplementMethodTask>,
)

data class InterceptMethodTask(
    val methodName: String,
    val jvmMethodDescriptor: String,
)

data class ImplementMethodTask(
    /**
     * The fully qualified name of the closes in the
     * hierarchy from the
     * [InterceptClassTask.classNameToIntercept]
     * class which declared the method
     */
    val declaringClassName : String,

    val methodName: String,
    val jvmMethodDescriptor: String,
)
