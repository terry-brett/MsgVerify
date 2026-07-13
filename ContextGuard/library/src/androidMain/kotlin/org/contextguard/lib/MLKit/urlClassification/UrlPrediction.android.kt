package org.contextguard.lib.MLKit.urlClassification

import android.content.Context
import org.contextguard.lib.MLKit.urlClassification.constants.Constants
import org.contextguard.lib.MLKit.urlClassification.models.Scaler
import java.nio.ByteBuffer
import java.nio.ByteOrder

actual class UrlPrediction actual constructor(private val platformContext: Any) {
    private val context: Context = platformContext as Context

    actual suspend fun makePrediction(url: String): Float {
        val loadAssets = LoadModel(context)
        val modelDesc = loadAssets.getUrlModelDesc()

        val scaler = Scaler(mean = Constants.SCALER_MEAN, scale = Constants.SCALER_SCALE)
        val featuresMap = UrlVerifierHelper(url).extractUrlFeatures()
        val rawFeatures = Constants.URL_FEATURE_KEYS.map { featuresMap[it]?.toFloat() ?: 0f }

        return UrlVerifier.getInstance().makePrediction(
            modelDesc = modelDesc,
            data = floatArrayToByteArray(scaler.transform(rawFeatures)),
        )
    }
}

private fun floatArrayToByteArray(floatArray: FloatArray): ByteArray {
    val byteBuffer = ByteBuffer.allocateDirect(floatArray.size * 4).apply {
        order(ByteOrder.nativeOrder())
    }
    byteBuffer.asFloatBuffer().put(floatArray)
    byteBuffer.rewind()
    val byteArray = ByteArray(byteBuffer.remaining())
    byteBuffer.get(byteArray)
    return byteArray
}
