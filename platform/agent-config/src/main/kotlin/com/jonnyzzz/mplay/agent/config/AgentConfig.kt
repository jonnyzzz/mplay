package com.jonnyzzz.mplay.agent.config

import java.nio.file.Path

data class AgentConfig(
    val classpath: List<Path>,
    val classesToProcess: List<AgentConfigClassTask>,
)

data class AgentClassMakeOpenTask(
    val className: String
)

class AgentConfigClassTask(

)

