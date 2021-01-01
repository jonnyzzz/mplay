package com.jonnyzzz.mplay.agent.config

/**
 * TODO: How should we deal with config classpath here?
 * Is it fine to assume these classes are included into the agent? Or mayby in the app classpath?
 */
data class AgentConfig(
    val classesToRecordEvents: List<InterceptClassTask>,
    //we also need the set of classes, where methods would be made non-final
)

class InterceptClassTask(
    val name: String
)

