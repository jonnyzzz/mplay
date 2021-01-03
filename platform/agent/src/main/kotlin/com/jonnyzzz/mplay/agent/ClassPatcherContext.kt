package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.runtime.MPlayRecorder
import org.objectweb.asm.Type

class ClassPatcherContext {
    val mplayFieldName = "______jonnyzzzMPlayRecorder" // we use unicode symbols to avoid a clash

    val mplayRecorderType = MPlayRecorder::class.java
    val mplayFieldDescriptor = Type.getDescriptor(mplayRecorderType)

    val mplayStaticGetInstance = staticMethod<MPlayRecorder>("getInstance")
    val mplayRecorderOnEnter = virtualMethod<MPlayRecorder>("onMethodEnter")

    val methodCallRecorderType = Type.getType(mplayRecorderOnEnter.returnType)
    val methodCallCommitWithResult = virtualMethod(mplayRecorderOnEnter.returnType, "commitWithResult")
}
