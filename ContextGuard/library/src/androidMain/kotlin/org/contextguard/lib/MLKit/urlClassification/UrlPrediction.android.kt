package org.contextguard.lib.MLKit.urlClassification

import android.content.Context
import org.contextguard.lib.MLKit.urlClassification.LoadModel
import org.contextguard.lib.MLKit.urlClassification.constants.Constants
import org.contextguard.lib.MLKit.urlClassification.models.Scaler
import java.nio.ByteBuffer
import java.nio.ByteOrder


actual class UrlPrediction actual constructor(private val platformContext: Any) {
    private val context: Context = platformContext as Context

    actual suspend fun makePrediction(url: String): Float {
        val loadAssets = LoadModel(context)
        val modelDesc = loadAssets.getModelDesc()

        val scaler = Scaler(mean = Constants.SCALER_MEAN, scale = Constants.SCALER_SCALE)

        // Extract features
        val featuresMap = UrlVerifierHelper(url).extractUrlFeatures()

        // Order the features to match model input
        val orderedKeys = Constants.URL_FEATURE_KEYS

        val rawFeatures = orderedKeys.map { featuresMap[it]?.toFloat() ?: 0f }

        val result =  UrlVerifier().makePrediction(
            modelDesc = modelDesc,
            data = floatArrayToByteArray(scaler.transform(rawFeatures)),
        )

       return result
    }
}

fun floatArrayToByteArray(floatArray: FloatArray): ByteArray {
    // Allocate a direct ByteBuffer to hold the float values.
    // Each float is 4 bytes, so multiply the number of floats by 4.
    val byteBuffer = ByteBuffer.allocateDirect(floatArray.size * 4).apply {
        // Set the byte order. Use nativeOrder() for compatibility with
        // native code like TensorFlow Lite, or BIG_ENDIAN/LITTLE_ENDIAN
        // if a specific order is required.
        order(ByteOrder.nativeOrder())
    }

    // Wrap the ByteBuffer around a FloatBuffer to put floats directly.
    val floatBuffer = byteBuffer.asFloatBuffer()

    // Put the FloatArray into the FloatBuffer.
    floatBuffer.put(floatArray)

    // Rewind the ByteBuffer's position to the beginning before converting to byte array.
    byteBuffer.rewind()

    // Create a ByteArray and copy the ByteBuffer's content into it.
    val byteArray = ByteArray(byteBuffer.remaining())
    byteBuffer.get(byteArray)

    return byteArray
}
