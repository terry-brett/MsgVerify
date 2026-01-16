package org.contextguard.lib.MLKit.messageClassification

expect class MessagePrediction(platformContext: Any) {
    /**
     * Checks whether the message is spam or ham and
     * @param message to be analyzed
     * @return Float probability of message being malicious
     */
    suspend fun getSpamPrediction(message: String): Float
}