package org.contextguard.lib.MLKit.urlClassification

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.contextguard.lib.MLKit.urlClassification.constants.Constants
import org.contextguard.lib.MLKit.urlClassification.models.Scaler
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.getBytes
import kotlin.experimental.ExperimentalNativeApi

actual class UrlPrediction actual constructor(platformContext: Any) {
    actual suspend fun makePrediction(url: String): Float {
        val loadAssets = LoadModel()
        val modelDesc = loadAssets.getUrlModelDesc()

        val scaler = Scaler(mean = Constants.SCALER_MEAN, scale = Constants.SCALER_SCALE)
        val featuresMap = UrlVerifierHelper(url).extractUrlFeatures()
        val rawFeatures = Constants.URL_FEATURE_KEYS.map { featuresMap[it]?.toFloat() ?: 0f }

        return UrlVerifier.getInstance().makePrediction(
            modelDesc = modelDesc,
            data = floatArrayToByteArray(scaler.transform(rawFeatures))
        )
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
private fun floatArrayToByteArray(floatArray: FloatArray): ByteArray {
    if (floatArray.isEmpty()) return ByteArray(0)

    memScoped {
        val cFloatArray = allocArrayOf(*floatArray)
        val nsData = NSData.create(
            bytes = cFloatArray,
            length = (floatArray.size * 4).toULong()
        )
        val byteArray = ByteArray(nsData.length.toInt())
        byteArray.usePinned { pinnedArray ->
            nsData.getBytes(pinnedArray.addressOf(0), length = nsData.length)
        }
        return byteArray
    }
}
