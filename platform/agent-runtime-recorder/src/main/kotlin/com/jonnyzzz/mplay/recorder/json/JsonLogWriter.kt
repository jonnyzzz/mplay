package com.jonnyzzz.mplay.recorder.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.Closeable
import java.nio.file.Files
import java.nio.file.Path

private val om = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)

class JsonLogWriter(
    private val targetFile: Path
) : Closeable {
    private val writer = run {
        targetFile.parent?.let { runCatching { Files.createDirectories(it) } }
        val stream = Files.newBufferedWriter(targetFile, Charsets.UTF_8)
        om.writerWithDefaultPrettyPrinter().createGenerator(stream)
    }.also {
        it.writeStartObject()
        it.writeArrayFieldStart("events")
    }

    override fun close() {
        runCatching {
            writer.writeEndArray()
            writer.writeEndObject()
        }

        runCatching {
            writer.close()
        }
    }

    private fun writeMessage(type: String, message: Any) {
        val result = om.createObjectNode()
        result.put("type", type)
        val node: ObjectNode = om.valueToTree(message)

        for (element in node.fieldNames()) {
            require(element != "type") { "Unexpected field for $message" }
            result.replace(element, node[element])
        }

        writer.writeObject(result)
    }

    fun writeConstructorCall(message: ConstructorCallMessage) = writeMessage("ctor", message)
    fun writeMethodCall(message: MethodCallMessage) = writeMessage("call", message)
    fun writeMethodResult(message: MethodCallResult) = writeMessage("ret", message)
}
