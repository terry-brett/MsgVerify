package org.contextguard.lib.MLKit

import org.contextguard.lib.MLKit.constants.Constants
import org.contextguard.lib.MLKit.models.Scaler
import kotlin.math.exp
import de.voize.pytorch_lite_multiplatform.IValue
import de.voize.pytorch_lite_multiplatform.Tensor
import de.voize.pytorch_lite_multiplatform.TorchModule
import de.voize.pytorch_lite_multiplatform.plmScoped

class UrlVerifier {
    fun makePrediction(
        modelPath: String,
        url: String
    ): Float {
        val scaler = Scaler(mean = Constants.SCALER_MEAN, scale = Constants.SCALER_SCALE)

        val model = TorchModule(modelPath)

        // Extract features
        val featuresMap = UrlVerifierHelper(url).extractUrlFeatures()

        // Order the features to match model input
        val orderedKeys = Constants.URL_FEATURE_KEYS

        val rawFeatures = orderedKeys.map { featuresMap[it]?.toFloat() ?: 0f }
        val input = scaler.transform(rawFeatures)

        return plmScoped {
            val inputTensor = Tensor.fromBlob(
                data = input,
                shape = longArrayOf(1, 25),
                scope = this
            )
            val inputIValue = IValue.from(inputTensor)
            val output = model.forward(inputIValue)
            val outputTensor = output.toTensor()
            val outputData = outputTensor.getDataAsFloatArray()

            sigmoid(outputData.first()) * 100
        }
    }
}

private fun sigmoid(x: Float): Float = (1 / (1 + exp(-x)))
