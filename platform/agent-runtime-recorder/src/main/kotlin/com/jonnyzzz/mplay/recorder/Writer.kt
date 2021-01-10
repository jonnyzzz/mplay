package com.jonnyzzz.mplay.recorder

import java.nio.file.Path


class MethodCallsWriterPaths(
    private val targetDir: Path
) {
    private val enumerator = ObjectsEnumerator<Thread>()

    fun pathForThread(thread: Thread, suffix: String = ""): Path {
        val id = enumerator.enumerate(thread)
        return targetDir.resolve("log-thread-$id$suffix")
    }

    override fun toString() = "MethodCallsWriterPaths(targetDir=$targetDir)"
}
