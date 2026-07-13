package org.contextguard.lib.MLKit.messageClassification

import org.contextguard.lib.MLKit.messageClassification.tokenizer.WordPieceTokenizer
import org.contextguard.lib.MLKit.messageClassification.tokenizer.loadVocab

actual class MessagePrediction actual constructor(platformContext: Any) {
    actual suspend fun isSpam(message: String): Boolean {
        val tokenizer = getOrCreateTokenizer()

        val loadModel = LoadModel()
        val modelDesc = loadModel.getTextModelDesc()

        val (inputIds, attentionMask) = tokenizer.encode(
            message = message,
            maxLen = MAX_LEN
        )

        val probabilities = MessageVerifier.getInstance().makePrediction(
            modelDesc = modelDesc,
            inputIds = inputIds,
            attentionMask = attentionMask
        )

        val notSpamScore = probabilities[0]
        val spamScore = probabilities[1]

        return spamScore > notSpamScore
    }

    companion object {
        private var cachedTokenizer: WordPieceTokenizer? = null

        private suspend fun getOrCreateTokenizer(): WordPieceTokenizer {
            cachedTokenizer?.let { return it }
            val vocab = loadVocab()
            cachedTokenizer = WordPieceTokenizer(vocab)
            return cachedTokenizer!!
        }
    }
}
