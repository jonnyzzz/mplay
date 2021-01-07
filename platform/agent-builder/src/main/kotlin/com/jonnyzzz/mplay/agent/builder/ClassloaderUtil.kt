package com.jonnyzzz.mplay.agent.builder

fun <T> Class<T>.loadClassBytes() : ByteArray {
    val stream = classLoader.getResourceAsStream(name.replace('.', '/') + ".class")
        ?: error("Failed to find class resource for $name in $classLoader")

    return stream.use { it.readAllBytes() }
}
