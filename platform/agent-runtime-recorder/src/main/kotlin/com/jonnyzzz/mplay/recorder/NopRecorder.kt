package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.runtime.MPlayMethodCallRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayMethodResultRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayInstanceRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayRunningMethodRecorder

object NopRecorder : MPlayInstanceRecorder, MPlayRunningMethodRecorder, MPlayMethodCallRecorder, MPlayMethodResultRecorder {
    override fun newRunningMethodRecorder() = this
    override fun newMethodResultRecorder() = this
    override fun commit() = Unit
    override fun newMethodRecorder(methodName: String, descriptor: String) = this
}
