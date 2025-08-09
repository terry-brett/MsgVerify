package org.contextguard.lib

import android.content.Context
import dev.kursor.ktensorflow.api.ModelDesc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.contextguard.lib.library.generated.resources.Res
import java.io.File

actual class LoadModel(private val context: Context) {
    actual suspend fun getModelDesc(): ModelDesc {
        val bytes = Res.readBytes("files/model_lite.tflite")
        val tmpFile = File.createTempFile("prefix", "suffix", context.cacheDir)
        tmpFile.writeBytes(bytes)
        val modelDesc = ModelDesc.File(tmpFile)

        return modelDesc
    }
}
