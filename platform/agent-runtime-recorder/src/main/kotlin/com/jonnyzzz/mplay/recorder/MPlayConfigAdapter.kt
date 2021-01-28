package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.config.MPlayConfiguration
import com.jonnyzzz.mplay.config.MPlayConfigurationDriver

class MPlayConfigAdapter(
    private val config: MPlayConfiguration<*>,
    private val descriptor: String,
) {
    fun resolveDriverCall(args: List<Any?>): MPlayConfigDriverAdapter? {
        val constructorTypes = methodDescriptorParamsToTypes(descriptor, config.javaClass.classLoader).toTypedArray()
        val driver = config.javaClass.getMethod("newDriver", *constructorTypes).invoke(config, *args.toTypedArray())
        val configurationDriver = driver as? MPlayConfigurationDriver<*> ?: return null
        return MPlayConfigDriverAdapter(config, configurationDriver)
    }
}

class MPlayConfigDriverAdapter(
    private val config: MPlayConfiguration<*>,
    private val driver: MPlayConfigurationDriver<*>,
) {
    fun mapConstructorParamsForSerialization(args: List<Any?>) = driver.mapConstructorParamsForSerialization(args)


}

private fun CharIterator.readUntil(x: Char) = buildString {
    while(hasNext()) {
        val next = nextChar()
        if (next == x) break
        append(next)
    }
}

private fun methodDescriptorParamsToTypes(descriptor: String, classLoader: ClassLoader) : List<Class<*>> {
    val result = mutableListOf<Class<*>>()
    val chars = descriptor.iterator()
    require(chars.nextChar() == '(') { "Method signature must start from '(' " }

    var arrayArity = 0
    while(chars.hasNext()) {
        val next = chars.next()
        @Suppress("RemoveRedundantQualifierName")
        var nextType : Class<*> = when (next) {
            ')' -> break
            'I' -> java.lang.Integer.TYPE
            'V' -> java.lang.Void.TYPE
            'Z' -> java.lang.Boolean.TYPE
            'B' -> java.lang.Byte.TYPE
            'C' -> java.lang.Character.TYPE
            'S' -> java.lang.Short.TYPE
            'D' -> java.lang.Double.TYPE
            'F' -> java.lang.Float.TYPE
            'J' -> java.lang.Long.TYPE
            'L' -> classLoader.loadClass(chars.readUntil(';').replace('/', '.'))
            '[' -> {
                arrayArity++
                null
            }
            else -> error("Unexpected char $next in $descriptor")
        } ?: continue

        if (arrayArity > 0) {
            nextType = java.lang.reflect.Array.newInstance(nextType, arrayArity).javaClass
        }
        arrayArity = 0
        result += nextType
    }

    return result.toList()
}
