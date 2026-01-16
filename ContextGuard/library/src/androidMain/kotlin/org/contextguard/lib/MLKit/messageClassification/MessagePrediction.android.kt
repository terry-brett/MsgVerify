package org.contextguard.lib.MLKit.messageClassification
import android.content.Context
import org.contextguard.lib.MLKit.messageClassification.tokenizer.WordPieceTokenizer
import org.contextguard.lib.MLKit.messageClassification.tokenizer.loadVocab
import kotlin.math.log

actual class MessagePrediction actual constructor(private val platformContext: Any) {
    private val context: Context = platformContext as Context

    actual suspend fun getSpamPrediction(message: String): Float {
        val vocab = loadVocab()

        val tokenizer = WordPieceTokenizer(vocab)

        val loadModel = LoadModel(context)
        val modelDesc = loadModel.getTextModelDesc()

        val (inputIds, attentionMask) = tokenizer.encode(
            message = message,
            maxLen = 128
        )

        val logits = MessageVerifier().makePrediction(
            modelDesc = modelDesc,
            inputIds = inputIds,
            attentionMask = attentionMask
        )

        println("prediction is ${logits}")

        return 0f
    }
}