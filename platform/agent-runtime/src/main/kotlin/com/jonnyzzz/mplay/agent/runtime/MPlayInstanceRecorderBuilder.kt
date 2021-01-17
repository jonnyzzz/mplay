package com.jonnyzzz.mplay.agent.runtime

/**
 * The main entry point for the MPlay recorder injection,
 * an instance of this type is created via the
 * [MPlayRecorderFactory] and the builder configuration
 * is executed from every constructor of a class,
 * where the recording is needed
 *
 * Usually created from
 * [MPlayRecorderBuilderFactory.newRecorderBuilderFactory].
 * This instance can be reused between different objects
 *
 * @see MPlayRecorderBuilderFactory.newRecorderBuilderFactory
 */
interface MPlayInstanceRecorderBuilder {
    /**
     * Notifies the implementation with the class,
     * that we are about to record.
     *
     * @param recordingClassName - the fully qualified name of the class
     */
    fun visitRecordingClassName(recordingClassName: String) = Unit

    /**
     * Visits the class fully qualified name of the configuration
     * that may provide hints on the methods recording of the
     * given class.
     *
     * @param configurationClassName - the fully qualified name of
     *                                 the configuration class
     *
     * May not be called if there is no configuration class associated
     */
    fun visitConfigurationClassName(configurationClassName: String) = Unit

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
    fun newConstructorRecorder(descriptor: String) : MPlayConstructorRecorder
}
