package org.contextguard.lib.heuristics.messageLabelling

class MessageReasoningLabels (val message: String) {
    init {
        private val rawMessage: String = message
        private val message: String = normalise(message)
        private val labels: MutableList<String> = mutableListOf()
    }


}