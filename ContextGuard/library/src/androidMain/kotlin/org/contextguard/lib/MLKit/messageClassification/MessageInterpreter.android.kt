package org.contextguard.lib.MLKit.messageClassification

import android.content.Context
import org.contextguard.lib.MLKit.messageClassification.constants.Labels
import org.contextguard.lib.MLKit.messageClassification.tokenizer.WordPieceTokenizer
import org.contextguard.lib.MLKit.messageClassification.tokenizer.loadVocab

actual class MessageInterpreter actual constructor(private val platformContext: Any) {
    private val context: Context = platformContext as Context

    actual suspend fun getListOfReasonsFromMessage(
        message: String,
        hypothesis: String
    ): List<String> {
        val vocab = loadVocab()
        val tokenizer = WordPieceTokenizer(vocab)

        val loadAssets = LoadModel(context)
        val modelDesc = loadAssets.getTextModelDesc()

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
        val predicted = labels[logits.indices.maxBy { logits[it] }]

        return listOf(predicted)
    }
}
