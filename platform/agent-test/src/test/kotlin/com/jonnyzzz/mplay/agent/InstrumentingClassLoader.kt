package com.jonnyzzz.mplay.agent

inline fun <reified Y> ClassLoader.loadClassByName(): Class<*> {
    val clazz = loadClass(Y::class.java.name)
    require(clazz !== Y::class.java) {
        "Loaded class $clazz has the same classloader as ${Y::class.java}!"
    }
    return clazz
}

class InstrumentingClassLoader(
    private val interceptor: ClassInterceptor,
    private val realLoader: ClassLoader = InstrumentingClassLoader::class.java.classLoader,
) : ClassLoader(null) {

    override fun findClass(name: String): Class<*> {
        if (name.startsWith("kotlin.")) {
            return realLoader.loadClass(name)
        }

        val resource = name.replace('.', '/') + ".class"
        val resourceStream = realLoader.getResourceAsStream(resource)
            ?: throw ClassNotFoundException("Failed to find class $name at $resource")

        val originalBytes = resourceStream.use { it.readBytes() }
        val classBytes = interceptor.intercept(
            name,
            originalBytes,
        ) ?: originalBytes

        if (originalBytes.contentEquals(classBytes)) {
            println("Loading original class $name")
        } else {
            println("Loading patched class $name")
        }

        return defineClass(name, classBytes, 0, classBytes.size, null)
    }
}
