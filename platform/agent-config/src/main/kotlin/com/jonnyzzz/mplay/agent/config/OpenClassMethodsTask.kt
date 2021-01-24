package com.jonnyzzz.mplay.agent.config

import kotlinx.serialization.Serializable

@Serializable
data class OpenClassMethodsTask(
    /**
     * Fully Qualified name of the class to record method calls
     */
    val classNameToIntercept: String,

    val methodsToOpen: List<OpenMethodTask>
)

@Serializable
data class OpenMethodTask(
    val methodRef: MethodRef,
)
