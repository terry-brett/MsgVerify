package org.contextguard.lib.MLKit.messageClassification

expect class MessagePrediction(platformContext: Any) {
    /**
     * @param message The message to be analyzed.
     * @return True if the message is classified as spam, false otherwise.
     */
    suspend fun isSpam(message: String): Boolean
}