package com.jonnyzzz.mplay.recorder.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode

data class ConstructorCallMessage (
    val recordingClass: String,
    val instanceId: Int,

    val descriptor: String,
    val parameters: ArrayNode,
)

data class MethodCallMessage (
    val callId: Long,
    val instanceId: Int,

    val name: String,
    val descriptor: String,
    val parameters: ArrayNode,
)

data class MethodCallResult(
    val callId: Long,

    val durationNanos: Long,

    val result: JsonNode? = null,
    val exception: ExceptionMessage? = null,
)

data class ExceptionMessage(
    val type: String,
)
