package com.jonnyzzz.mplay.agent.runtime

/**
 * This interface allows the implementation to record the
 * method execution start/finish events.
 *
 * It is usually created via [MPlayMethodCallRecorder.newRunningMethodRecorder]
 * call just before the original method starts to execute
 *
 * @see MPlayMethodCallRecorder.newRunningMethodRecorder
 */
interface MPlayRunningMethodRecorder {
    /**
     * This method is executed once the recording method completes
     * the execution, before the result (possibly void) returned or an
     * exception is thrown
     */
    fun newMethodResultRecorder() : MPlayMethodResultRecorder
}
