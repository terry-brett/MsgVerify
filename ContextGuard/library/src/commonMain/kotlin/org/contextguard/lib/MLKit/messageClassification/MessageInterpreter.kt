package org.contextguard.lib.MLKit.messageClassification

expect class MessageInterpreter(platformContext: Any) {
    suspend fun getListOfReasonsFromMessage(
        message: String,
        hypothesis: String = "The text demands personal information"
    ): List<String>
}
