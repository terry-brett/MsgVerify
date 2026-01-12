package org.contextguard.lib.heuristics.messageLabelling

fun normalise(message: String): String {
    return message
        .lowercase()
        .replace(Regex("\\s+"), " ")
        .trim()
}

