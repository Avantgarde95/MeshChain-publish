package com.github.avantgarde95.bc3d.common

class SimpleURI(
        val scheme: String = "http",
        val host: String,
        val port: Int = -1,
        val path: String = ""
) {
    override fun toString(): String {
        val schemeString = "$scheme://"
        val hostString = host

        val portString = when {
            port < 0 -> ""
            else -> ":$port"
        }

        val pathString = when {
            path.isEmpty() -> ""
            else -> "/$path"
        }

        return schemeString + hostString + portString + pathString
    }
}
