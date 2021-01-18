package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.agent.config.MethodRef
import com.jonnyzzz.mplay.agent.config.ctor
import org.objectweb.asm.Type
import java.lang.reflect.Constructor
import java.lang.reflect.Method


fun Method.toMethodRef() = MethodRef(name, Type.getMethodDescriptor(this))

fun <T> Constructor<T>.toMethodRef() = MethodRef.ctor(Type.getConstructorDescriptor(this))
