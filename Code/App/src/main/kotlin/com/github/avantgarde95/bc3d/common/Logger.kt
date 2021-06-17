package com.github.avantgarde95.bc3d.common

object Logger {
    val logEvent = SimpleEvent<String>()
    val allStrings = mutableListOf<String>()

    init {
        logEvent.addListener {
            allStrings.add(it)
        }
    }

    fun addString(string: String, ensureNewline: Boolean = true) {
        logEvent.fire(when {
            ensureNewline && !string.endsWith('\n') -> "$string\n"
            else -> string
        })
    }
}
