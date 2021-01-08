package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.runtime.*
import org.objectweb.asm.Type

class ClassPatcherContext {
    val mplayNewRecorderBuilder = staticMethod<MPlayRecorderFactory>(MPlayRecorderFactory::newRecorderBuilder)

    @Suppress("PrivatePropertyName", "unused")
    private val test_mplayNewRecorderBuilder = require(mplayNewRecorderBuilder.returnType == MPlayRecorderBuilder::class.java)

    val mplayRecorderBuilderVisitRecordingClassName = method<MPlayRecorderBuilder>(MPlayRecorderBuilder::visitRecordingClassName)
    val mplayRecorderBuilderVisitConfigClassName = method<MPlayRecorderBuilder>(MPlayRecorderBuilder::visitConfigurationClassName)
    val mplayRecorderBuilderVisitInstance = method<MPlayRecorderBuilder>(MPlayRecorderBuilder::visitInstance)
    val mplayRecorderBuilderVisitDescriptor = method<MPlayRecorderBuilder>(MPlayRecorderBuilder::visitConstructorDescriptor)
    val mplayRecorderBuilderVisitComplete = method<MPlayRecorderBuilder>(MPlayRecorderBuilder::visitConstructorParametersComplete)

    @Suppress("PrivatePropertyName", "unused")
    private val test_mplayRecorderBuilderVisitComplete = require(mplayRecorderBuilderVisitComplete.returnType == MPlayRecorder::class.java)

    val mplayFieldName = "______jonnyzzzMPlayRecorder" // we use unicode symbols to avoid a clash
    val mplayFieldDescriptor = Type.getDescriptor(MPlayRecorder::class.java)

    val mplayRecorderOnEnter = method<MPlayRecorder>(MPlayRecorder::onMethodEnter)

    private val typeSortToVisitMethods = mapOf(
        Type.BOOLEAN to  method<MPlayValuesVisitor>(MPlayValuesVisitor::visitBoolean),
        Type.CHAR    to  method<MPlayValuesVisitor>(MPlayValuesVisitor::visitChar),
        Type.BYTE    to  method<MPlayValuesVisitor>(MPlayValuesVisitor::visitByte),
        Type.SHORT   to  method<MPlayValuesVisitor>(MPlayValuesVisitor::visitShort),
        Type.INT     to  method<MPlayValuesVisitor>(MPlayValuesVisitor::visitInt),
        Type.FLOAT   to  method<MPlayValuesVisitor>(MPlayValuesVisitor::visitFloat),
        Type.LONG    to  method<MPlayValuesVisitor>(MPlayValuesVisitor::visitLong),
        Type.DOUBLE  to  method<MPlayValuesVisitor>(MPlayValuesVisitor::visitDouble),
        Type.ARRAY   to  method<MPlayValuesVisitor>(MPlayValuesVisitor::visitObject),
        Type.OBJECT  to  method<MPlayValuesVisitor>(MPlayValuesVisitor::visitObject),
    )
    val visitException = method<MPlayExceptionVisitor>(MPlayExceptionVisitor::visitException)

    fun mplayVisitMethod(type: Type): MethodCallInfo = typeSortToVisitMethods[type.sort] ?: error("Unexpected type: $type")

    val methodCallParametersComplete = method<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::visitParametersComplete)
    val methodCallCommit = method<MPlayMethodResultRecorder>(MPlayMethodResultRecorder::commit)
}
