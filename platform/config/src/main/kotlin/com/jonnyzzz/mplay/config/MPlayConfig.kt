package com.jonnyzzz.mplay.config

/**
 * A marker annotation of the class which implements
 * the [MPlayConfiguration]. The class can be a generic
 * one if the original type has generic parameters.
 */
annotation class MPlayConfig

/**
 * The entry point for the MPlay configuration,
 * this interface can be implemented as a class with
 * default constructor or as a Kotlin Object.
 *
 * The [T] is the type of the class, which will be
 * enhanced to record all calls from it.
 *
 * This interface defines the configuration
 * of the MPLay
 */
interface MPlayConfiguration<T> {
    /**
     * Specified **inclusive** upper bound in base classes
     * hierarchy to limit the number of methods that will
     * be intercepted. The [Any] or [java.lang.Object] is
     * the default value
     */
    val upperLimit: Class<*> get() = java.lang.Object::class.java
}
