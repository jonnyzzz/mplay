package com.jonnyzzz.mplay.config

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

}
