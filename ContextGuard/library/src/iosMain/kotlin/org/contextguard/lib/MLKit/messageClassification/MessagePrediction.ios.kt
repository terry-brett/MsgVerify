package org.contextguard.lib.MLKit.messageClassification

import org.contextguard.lib.MLKit.messageClassification.tokenizer.WordPieceTokenizer
import org.contextguard.lib.MLKit.messageClassification.tokenizer.loadVocab

actual class MessagePrediction actual constructor(platformContext: Any) {
    actual suspend fun isSpam(message: String): Boolean {
        val vocab = loadVocab()

        val tokenizer = WordPieceTokenizer(vocab)

        val loadModel = LoadModel()
        val modelDesc = loadModel.getTextModelDesc()

        val (inputIds, attentionMask) = tokenizer.encode(
            message = message,
            maxLen = 128
        )

        val probabilities = MessageVerifier().makePrediction(
            modelDesc = modelDesc,
            inputIds = inputIds,
            attentionMask = attentionMask
        )

        val notSpamScore = probabilities[0]
        val spamScore = probabilities[1]

        return spamScore > notSpamScore
    }
}
