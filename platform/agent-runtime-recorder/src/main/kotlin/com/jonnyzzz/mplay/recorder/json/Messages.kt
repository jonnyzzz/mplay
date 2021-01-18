package com.jonnyzzz.mplay.recorder.json

data class ConstructorCallMessage (
    val recordingClass: String,
    val instanceId: Int,

    val descriptor: String,
    val parameters: List<Any?>,
)

data class MethodCallMessage (
    val callId: Long,
    val instanceId: Int,

    val name: String,
    val descriptor: String,
    val parameters: List<Any?>,
)

data class MethodCallResult(
    val callId: Long,

    val durationNanos: Long,

    val result: Any? = null,
    val exception: ExceptionMessage? = null,
)

data class ExceptionMessage(
    val type: String,
)
