package com.jonnyzzz.mplay.agent.config

import kotlinx.serialization.Serializable

@Serializable
data class MethodRef(
    val methodName: String,
    val descriptor: String,
) {
    override fun toString() = "$methodName $descriptor"
    companion object
}

fun MethodRef.Companion.ctor(descriptor: String) = MethodRef("<init>", descriptor)
