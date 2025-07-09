package org.contextguard.lib.MLKit

import android.content.Context
import de.voize.pytorch_lite_multiplatform.TorchModule
import org.contextguard.lib.LoadModel


actual class UrlPrediction actual constructor(private val platformContext: Any) {
    private val context: Context = platformContext as Context

    actual suspend fun makePrediction(url: String): String {
        val loadModel = LoadModel(context)
        val modelPath = loadModel.getModelPath()

        val model = TorchModule(modelPath)

        println("model is $model")
        return ""
    }
}