package com.jonnyzzz.mplay.agent.builder


fun Array<String>.param(key: String) = mapNotNull { it.substringOrNull("--$key=") }

fun String.substringOrNull(prefix: String): String? {
    return if (this.startsWith(prefix)) {
        this.removePrefix(prefix)
    } else {
        null
    }
}

fun String.trimAndNullIfBlank(): String? = this.trim().run { if (isNotEmpty()) this else null }

