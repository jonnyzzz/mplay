package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.runtime.MPlayMethodCallRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayRecorder
import org.objectweb.asm.Type

class ClassPatcherContext {
    val mplayFieldName = "______jonnyzzzMPlayRecorder" // we use unicode symbols to avoid a clash

    val mplayRecorderType = MPlayRecorder::class.java
    val mplayFieldDescriptor = Type.getDescriptor(mplayRecorderType)

    val mplayStaticGetInstance = staticMethod<MPlayRecorder>(MPlayRecorder.Companion::getInstance)
    val mplayRecorderOnEnter = virtualMethod<MPlayRecorder>(MPlayRecorder::onMethodEnter)

    private val typeSortToWriteMethods = mapOf(
        Type.BOOLEAN to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::visitBoolean),
        Type.CHAR    to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::visitChar),
        Type.BYTE    to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::visitByte),
        Type.SHORT   to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::visitShort),
        Type.INT     to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::visitInt),
        Type.FLOAT   to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::visitFloat),
        Type.LONG    to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::visitLong),
        Type.DOUBLE  to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::visitDouble),
        Type.ARRAY   to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::visitObject),
        Type.OBJECT  to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::visitObject),
    )

    fun mplayWriteMethod(type: Type): MethodCallInfo = typeSortToWriteMethods[type.sort] ?: error("Unexpected type: $type")

    val methodCallRecorderType = Type.getType(mplayRecorderOnEnter.returnType)
    val methodCallParametersComplete = virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::visitParametersComplete)
    val methodCallCommitWithResult = virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::commitWithResult)
    val methodCallCommitWithException = virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::commitWithException)
}
