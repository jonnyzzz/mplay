package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.runtime.*
import org.objectweb.asm.Type

class ClassPatcherContext {
    val mplayNewRecorderBuilder = staticMethod<MPlayRecorderFactory>(MPlayRecorderFactory::newRecorderBuilder)

    @Suppress("PrivatePropertyName", "unused")
    private val test_mplayNewRecorderBuilder = require(mplayNewRecorderBuilder.returnType == MPlayInstanceRecorderBuilder::class.java)

    val mplayRecorderBuilderVisitRecordingClassName = method<MPlayInstanceRecorderBuilder>(MPlayInstanceRecorderBuilder::visitRecordingClassName)
    val mplayRecorderBuilderVisitConfigClassName = method<MPlayInstanceRecorderBuilder>(MPlayInstanceRecorderBuilder::visitConfigurationClassName)
    val mplayNewConstructorRecorder = method<MPlayInstanceRecorderBuilder>(MPlayInstanceRecorderBuilder::newConstructorRecorder)
    val mplayNewConstructorCallRecorder = method<MPlayConstructorRecorder>(MPlayConstructorRecorder::newConstructorCallRecorder)

    val mplayRecorderBuilderVisitInstance = method<MPlayConstructorCallRecorder>(MPlayConstructorCallRecorder::visitInstance)
    val mplayRecorderBuilderVisitComplete = method<MPlayConstructorCallRecorder>(MPlayConstructorCallRecorder::newInstanceRecorder)

    @Suppress("PrivatePropertyName", "unused")
    private val test_mplayRecorderBuilderVisitComplete = require(mplayRecorderBuilderVisitComplete.returnType == MPlayInstanceRecorder::class.java)

    val mplayFieldName = "______jonnyzzzMPlayRecorder" // we use unicode symbols to avoid a clash
    val mplayFieldDescriptor = Type.getDescriptor(MPlayInstanceRecorder::class.java)

    val mplayRecorderOnEnter = method<MPlayInstanceRecorder>(MPlayInstanceRecorder::newMethodRecorder)

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

    val methodCallParametersComplete = method<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::newRunningMethodRecorder)
    val methodCallParametersCompleteType = Type.getType(MPlayRunningMethodRecorder::class.java)

    val methodCallCompleted = method<MPlayRunningMethodRecorder>(MPlayRunningMethodRecorder::newMethodResultRecorder)

    val methodCallCommit = method<MPlayMethodResultRecorder>(MPlayMethodResultRecorder::commit)
}
