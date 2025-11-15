package org.contextguard.lib.MLKit.urlClassification

import android.content.Context
import dev.kursor.ktensorflow.api.ModelDesc
import org.contextguard.lib.library.generated.resources.Res
import java.io.File

actual class LoadModel(private val context: Context) {
    actual suspend fun getUrlModelDesc(): ModelDesc {
        val bytes = Res.readBytes("files/model_lite.tflite")
        val tmpFile = File.createTempFile("prefix", "suffix", context.cacheDir)
        tmpFile.writeBytes(bytes)
        val modelDesc = ModelDesc.File(tmpFile)

        return modelDesc
    }
}