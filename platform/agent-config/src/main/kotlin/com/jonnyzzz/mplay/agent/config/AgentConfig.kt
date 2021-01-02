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

class InterceptClassTask(
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
    val methodsToRecord : List<InterceptMethodTask>
)

class InterceptMethodTask(
    val methodName: String,
    val jvmMethodDescriptor: String
    //here goes signature (for the case of overloads)
)
