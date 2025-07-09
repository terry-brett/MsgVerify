package org.contextguard

import android.content.Context
import de.voize.pytorch_lite_multiplatform.TorchModule
import org.contextguard.lib.LoadModel

actual class UrlVerifier(private val context: Context) {
    actual suspend fun makePrediction(modelPath: String) {
        val modelPath = LoadModel(context).getModelPath()

        val model = TorchModule(modelPath)
    }
}