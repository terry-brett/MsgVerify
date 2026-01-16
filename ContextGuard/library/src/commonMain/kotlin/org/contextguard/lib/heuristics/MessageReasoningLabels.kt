package org.contextguard.lib.heuristics.messageLabelling

class MessageReasoningLabels(val message: String) {
    init {
        val rawMessage: String = message
        val message: String = normalise(message)
        val labels: MutableList<String> = mutableListOf()
    }


}