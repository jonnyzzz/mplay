package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.runtime.MPlayMethodCallRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayRecorder
import org.objectweb.asm.Type

class ClassPatcherContext {
    val mplayFieldName = "______jonnyzzzMPlayRecorder" // we use unicode symbols to avoid a clash

    val mplayRecorderType = MPlayRecorder::class.java
    val mplayFieldDescriptor = Type.getDescriptor(mplayRecorderType)

    val mplayStaticGetInstance = staticMethod<MPlayRecorder>(MPlayRecorder.Companion::getInstance.name)
    val mplayRecorderOnEnter = virtualMethod<MPlayRecorder>(MPlayRecorder::onMethodEnter.name)

    private val typeSortToWriteMethods = mapOf(
        Type.BOOLEAN to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::writeBoolean.name),
        Type.CHAR    to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::writeChar.name),
        Type.BYTE    to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::writeByte.name),
        Type.SHORT   to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::writeShort.name),
        Type.INT     to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::writeInt.name),
        Type.FLOAT   to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::writeFloat.name),
        Type.LONG    to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::writeLong.name),
        Type.DOUBLE  to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::writeDouble.name),
        Type.ARRAY   to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::writeObject.name),
        Type.OBJECT  to  virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::writeObject.name),
    )

    fun mplayWriteMethod(type: Type): MethodCallInfo = typeSortToWriteMethods[type.sort] ?: error("Unexpected type: $type")

    val methodCallRecorderType = Type.getType(mplayRecorderOnEnter.returnType)
    val methodCallCommitWithResult = virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::commitWithResult.name)
    val methodCallCommitWithException = virtualMethod<MPlayMethodCallRecorder>(MPlayMethodCallRecorder::commitWithException.name)
}
