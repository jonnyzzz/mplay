package com.jonnyzzz.mplay.agent.runtime

/**
 * Usually created via [MPlayInstanceRecorderBuilder.newConstructorRecorder]
 */
interface MPlayConstructorRecorder {
    /**
     * Factory method to return an actual constructor visitor
     * for a given type.
     *
     * @param descriptor - non-generic constructor signature in the JVM format,
     *                     e.g. `(Ljava/lang/String;)V`.
     *
     * It is guaranteed, this method is called after all `visit*` methods of
     * this type were executed.
     */
    fun newConstructorCallRecorder(descriptor: String) : MPlayConstructorCallRecorder
}