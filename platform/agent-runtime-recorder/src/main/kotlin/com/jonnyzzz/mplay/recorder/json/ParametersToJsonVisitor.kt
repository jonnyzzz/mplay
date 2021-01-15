package com.jonnyzzz.mplay.recorder.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jonnyzzz.mplay.agent.runtime.MPlayValuesVisitor

private val om = jacksonObjectMapper()

class ParametersToJsonVisitor(
    private val base: MPlayValuesVisitor = object : MPlayValuesVisitor {}
) : MPlayValuesVisitor {
    private val params: ArrayNode = om.createArrayNode()

    fun toJson() = params

    override fun visitBoolean(v: Boolean): Boolean {
        params.add(v)
        return base.visitBoolean(v)
    }

    override fun visitChar(v: Char): Char {
        params.add(v.toString())
        return base.visitChar(v)
    }

    override fun visitByte(v: Byte): Byte {
        params.add(v.toInt())
        return base.visitByte(v)
    }

    override fun visitShort(v: Short): Short {
        params.add(v.toInt())
        return base.visitShort(v)
    }

    override fun visitInt(v: Int): Int {
        params.add(v)
        return base.visitInt(v)
    }

    override fun visitLong(v: Long): Long {
        params.add(v)
        return base.visitLong(v)
    }

    override fun visitFloat(v: Float): Float {
        params.add(v)
        return base.visitFloat(v)
    }

    override fun visitDouble(v: Double): Double {
        params.add(v)
        return base.visitDouble(v)
    }

    override fun visitObject(v: Any?): Any? {
        params.add(om.valueToTree(v) as JsonNode)
        return base.visitObject(v)
    }
}
