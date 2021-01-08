package com.jonnyzzz.mplay.agent.runtime

/**
 * The main entry point for the MPlay recorder injection,
 * an instance of this type is created via the
 * [MPlayRecorderFactory] and the builder configuration
 * is executed from every constructor of a class,
 * where the recording is needed
 */
interface MPlayRecorderBuilder : MPlayValuesVisitor {
    /**
     * Notifies the implementation with the class,
     * that we are about to record. In addition to the
     * text representation, it will pass actual instance
     * via the [visitInstance]. The [recordingClassName]
     * is the fully qualified name of the class
     */
    fun visitRecordingClassName(recordingClassName: String)

    /**
     * Visits the class fully qualified name of the configuration
     * that may provide hints on the methods recording of the
     * given class
     */
    fun visitConfigurationClassName(configurationClassName: String)

    /**
     * Visits which constructor is executed to create
     * the class, using the JVM method descriptor
     */
    fun visitConstructorDescriptor(descriptor: String)

    /**
     * Visits the instance of the class that is created
     * with methods recording enabled.
     *
     * NOTE, this is called from the constructor, so it
     * is probably not safe to use this instance methods
     * directly in the visitor.
     */
    fun visitInstance(instance: Any)

    /**
     * Marks that all constructor parameters were send.
     * Before calling this method, the implementation calls
     * the respective `visit*` methods from [MPlayValuesVisitor] to pass
     * all parameter values in the order of parameters in the
     * JVM metadata.
     *
     * Once the method is executed successfully or with an exception,
     * the implementation calls [MPlayMethodResultRecorder.commit]
     * to complete the method execution recording
     *
     * It is guaranteed this method is called the last
     */
    fun visitConstructorParametersComplete(): MPlayRecorder
}
