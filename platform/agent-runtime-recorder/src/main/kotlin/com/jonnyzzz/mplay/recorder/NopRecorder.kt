package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.runtime.MPlayMethodCallRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayMethodResultRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayRecorder

object NopRecorder : MPlayRecorder, MPlayMethodCallRecorder, MPlayMethodResultRecorder {
    override fun visitParametersComplete() = this
    override fun commit() = Unit
    override fun onMethodEnter(methodName: String, jvmMethodDescriptor: String) = this
}
