package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.recorder.json.JsonLogWriter
import java.io.Closeable
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class PerThreadWriter(
    private val root: Path,
    private val nameSuffix: String,
) : Closeable {
    private val counter = AtomicInteger(0)
    private val threadToWriter = ConcurrentHashMap<Long, JsonLogWriter>()

    fun writerForCurrentThread(): JsonLogWriter {
        val threadId = Thread.currentThread().id
        return threadToWriter.computeIfAbsent(threadId) {
            JsonLogWriter(root.resolve("thread-${counter.incrementAndGet()}$nameSuffix.json"))
        }
    }

    override fun close() {
        while (true) {
            val keys = threadToWriter.keys().toList()
            if (keys.isEmpty()) break
            keys.forEach {
                runCatching {
                    threadToWriter.remove(it)?.close()
                }
            }
        }
    }
}
