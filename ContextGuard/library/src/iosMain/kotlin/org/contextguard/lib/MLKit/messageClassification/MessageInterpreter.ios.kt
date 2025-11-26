package org.contextguard.lib.MLKit.messageClassification

import org.contextguard.lib.MLKit.messageClassification.tokenizer.WordPieceTokenizer
import org.contextguard.lib.MLKit.messageClassification.tokenizer.loadVocab

actual class MessageInterpreter actual constructor(platformContext: Any) {
    actual suspend fun getListOfReasonsFromMessage(
        message: String,
        hypothesis: String
    ): List<String> {
        val vocab = loadVocab()
        val tokenizer = WordPieceTokenizer(vocab)


        val loadModel = LoadModel()
        val modelDesc = loadModel.getTextModelDesc()

        val (inputIds, attentionMask) = tokenizer.encode(
            premise = message,
            hypothesis = hypothesis,
            maxLen = 128
        )

        val logits = MessageVerifier().makePrediction(
            modelDesc = modelDesc,
            inputIds = inputIds,
            attentionMask = attentionMask
        )

        val labels = listOf("entailment", "neutral", "contradiction")
        val predicted = labels[logits.indices.maxBy { logits[it] }]

        return listOf(predicted)
    }
}