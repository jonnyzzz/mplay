package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.runtime.MPlayRecorder
import org.objectweb.asm.Type
import java.lang.reflect.Modifier

class ClassPatcherContext {
    val mplayFieldName = "______jonnyzzzMPlayRecorder" // we use unicode symbols to avoid a clash

    val mplayRecorderType = MPlayRecorder::class.java
    val mplayFieldDescriptor = Type.getDescriptor(mplayRecorderType)
    val mplayTypeInternalName = Type.getInternalName(mplayRecorderType)

    val mplayTypeGetInstanceName = MPlayRecorder.Companion::getInstance.name
    val mplayTypeGetInstanceSignature = mplayRecorderType.methods
            .filter { Modifier.isStatic(it.modifiers) && Modifier.isPublic(it.modifiers) }
            .filter { it.name == mplayTypeGetInstanceName }
            .single().let { Type.getMethodDescriptor(it) }


    val mplayRecorderOnMethodEnterMethodName: String = MPlayRecorder::onMethodEnter.name
    val mplayRecorderOnMethodEnterMethod = mplayRecorderType.methods
        .filter { it.name == mplayRecorderOnMethodEnterMethodName }
        .filter { !Modifier.isStatic(it.modifiers) && Modifier.isPublic(it.modifiers) }
        .single()

    val mplayRecorderOnMethodEnterMethodSignature = Type.getMethodDescriptor(mplayRecorderOnMethodEnterMethod)

    val methodCallRecorderType = Type.getType(mplayRecorderOnMethodEnterMethod.returnType)
    val methodCallRecorderInternalName = methodCallRecorderType.internalName
}
