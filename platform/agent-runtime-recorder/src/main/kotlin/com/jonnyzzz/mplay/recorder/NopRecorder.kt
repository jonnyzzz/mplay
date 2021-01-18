package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.runtime.*

object NopRecorder : MPlayInstanceRecorder,
    MPlayRunningMethodRecorder,
    MPlayMethodCallRecorder,
    MPlayMethodResultRecorder,
    MPlayConstructorCallRecorder,
    MPlayConstructorRecorder{
    override fun newConstructorCallRecorder(descriptor: String) = this
    override fun newInstanceRecorder() = this
    override fun newRunningMethodRecorder() = this
    override fun newMethodResultRecorder() = this
    override fun newMethodRecorder(methodName: String, descriptor: String) = this
    override fun commit() = Unit
}
