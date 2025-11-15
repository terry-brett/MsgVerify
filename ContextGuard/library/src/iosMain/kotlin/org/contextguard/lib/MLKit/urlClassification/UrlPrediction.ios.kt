package org.contextguard.lib.MLKit.urlClassification

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.contextguard.lib.MLKit.urlClassification.LoadModel
import org.contextguard.lib.MLKit.urlClassification.constants.Constants
import org.contextguard.lib.MLKit.urlClassification.models.Scaler
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.getBytes
import kotlin.experimental.ExperimentalNativeApi

actual class UrlPrediction actual constructor(platformContext: Any) {
    actual suspend fun makePrediction(url: String): Float {
        val loadAssets = LoadModel()
        val modelDesc = loadAssets.getModelDesc()

        val scaler = Scaler(mean = Constants.SCALER_MEAN, scale = Constants.SCALER_SCALE)

        // extract features
        val featuresMap = UrlVerifierHelper(url).extractUrlFeatures()

        // order the features to match model input
        val orderedKeys = Constants.URL_FEATURE_KEYS

        val rawFeatures = orderedKeys.map { featuresMap[it]?.toFloat() ?: 0f }

        val result = UrlVerifier().makePrediction(
            modelDesc = modelDesc,
            data = floatArrayToByteArray(scaler.transform(rawFeatures))
        )

        return result
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
