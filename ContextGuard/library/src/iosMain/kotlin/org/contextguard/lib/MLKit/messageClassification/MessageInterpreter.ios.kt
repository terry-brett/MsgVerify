package org.contextguard.lib.MLKit.messageClassification

import org.contextguard.lib.MLKit.messageClassification.constants.Labels
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

        val labels = Labels.reasonLabels
        val k = 5 // We want the top 5 predictions

        val indexedLogits = logits.indices.map { index -> index to logits[index] }

        val sortedLogits = indexedLogits.sortedByDescending { it.second }

        val topKIndices = sortedLogits.take(k).map { it.first }

        val predictedLabels = topKIndices.map { index -> labels[index] }

        return predictedLabels
    }
}