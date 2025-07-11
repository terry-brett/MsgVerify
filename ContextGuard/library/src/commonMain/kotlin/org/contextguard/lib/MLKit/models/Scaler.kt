package org.contextguard.lib.MLKit.models

data class Scaler(
    val mean: List<Double>,
    val scale: List<Double>
) {
    fun transform(features: List<Float>): FloatArray {
        return features.mapIndexed { i, value ->
            ((value - mean[i]) / scale[i]).toFloat()
        }.toFloatArray()
    }
}
