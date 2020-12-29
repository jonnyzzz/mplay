package com.jonnyzzz.mplay.agent

import org.objectweb.asm.ClassReader

interface MPlayClass {
    fun newReader() : ClassReader

    companion object
}


inline fun <reified T> MPlayClass.Companion.fromJdk() = fromJdk(T::class.java)

fun <T> MPlayClass.Companion.fromJdk(clazz: Class<T>) = object: MPlayClass {
    private val clazzBytes by lazy {
        val stream = clazz.classLoader.getResourceAsStream(clazz.name.replace('.', '/') + ".class")
            ?: error("Failed to find class resource for ${clazz.name} in ${clazz.classLoader}")

        stream.use { it.readAllBytes() }
    }

    override fun newReader() = ClassReader(clazzBytes)
}

