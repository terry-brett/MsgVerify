package org.contextguard.lib.MLKit

import android.content.Context
import org.contextguard.lib.LoadModel


actual class UrlPrediction actual constructor(private val platformContext: Any) {
    private val context: Context = platformContext as Context

    actual suspend fun makePrediction(url: String): String {
        val loadAssets = LoadModel(context)
        val modelPath = loadAssets.getModelPath()

        val result =  UrlVerifier().makePrediction(
            modelPath = modelPath,
            url = url
        )
        val isSafe = if (result > 0.5f) "Phishing" else "Safe"

        val roundedNum = String.format("%.2f", result).toDouble()
        return "Prediction: $isSafe Probability of URL being a Phished URL: $roundedNum"
    }
}
