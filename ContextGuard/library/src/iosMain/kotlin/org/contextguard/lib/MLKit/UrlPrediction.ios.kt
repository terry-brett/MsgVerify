package org.contextguard.lib.MLKit

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.contextguard.lib.LoadModel
import org.contextguard.lib.MLKit.constants.Constants
import org.contextguard.lib.MLKit.models.Scaler
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.getBytes
import kotlin.experimental.ExperimentalNativeApi

actual class UrlPrediction actual constructor(platformContext: Any) {
    actual suspend fun makePrediction(url: String): String {
        val loadAssets = LoadModel()
        val modelDesc = loadAssets.getModelDesc()

        val scaler = Scaler(mean = Constants.SCALER_MEAN, scale = Constants.SCALER_SCALE)

        // Extract features
        val featuresMap = UrlVerifierHelper(url).extractUrlFeatures()

        // Order the features to match model input
        val orderedKeys = Constants.URL_FEATURE_KEYS

        val rawFeatures = orderedKeys.map { featuresMap[it]?.toFloat() ?: 0f }

        val result = UrlVerifier().makePrediction(
            modelDesc = modelDesc,
            data = floatArrayToByteArray(scaler.transform(rawFeatures))
        )
        val isSafe = if (result > 0.5f) "Phishing" else "Safe"

        // val roundedNum = String.format("%.2f", result).toDouble()
        val s = "Prediction: $isSafe Probability of URL being a Phished URL: $result"
        println(s)
        return "Prediction: $isSafe Probability of URL being a Phished URL: $result"
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
fun floatArrayToByteArray(floatArray: FloatArray): ByteArray {
    if (floatArray.isEmpty()) {
        return ByteArray(0)
    }

    memScoped {
        // Allocate native memory for the float array
        // Use the spread operator (*) to pass the FloatArray elements as varargs
        val cFloatArray = allocArrayOf(*floatArray)

        // Create NSData from the C array
        val nsData = NSData.create(
            bytes = cFloatArray,
            length = (floatArray.size * 4).toULong()
        )

        // Convert NSData to Kotlin ByteArray
        val byteArray = ByteArray(nsData.length.toInt())

        byteArray.usePinned { pinnedArray ->
            nsData.getBytes(pinnedArray.addressOf(0), length = nsData.length)
        }

        return byteArray
    }
}
