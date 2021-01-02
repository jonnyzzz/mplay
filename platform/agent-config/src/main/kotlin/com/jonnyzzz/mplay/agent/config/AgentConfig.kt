package com.jonnyzzz.mplay.agent.config

/**
 * TODO: How should we deal with config classpath here?
 * Is it fine to assume these classes are included into the agent? Or mayby in the app classpath?
 */
data class AgentConfig(
//    val configClasspath: List<String>,

    val classesToRecordEvents: List<InterceptClassTask>,
    //we also need the set of classes, where methods would be made non-final
)

class InterceptClassTask(
    /**
     * Class name in JVM format
     */
    val classNameToIntercept: String,

    /**
     * Class name in JVM format
     */
    val configClassName: String,

    val methodsToRecord : List<InterceptMethodTask>
)

class InterceptMethodTask(
    val methodName: String,
    val jvmMethodDescriptor: String
    //here goes signature (for the case of overloads)
)
