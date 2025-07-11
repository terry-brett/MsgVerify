package org.contextguard.lib

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.contextguard.lib.library.generated.resources.Res
import java.io.File


actual class LoadModel(private val context: Context) {
    actual suspend fun getModelPath(): String {

        val modelBytes = Res.readBytes("files/model_lite.ptl")
        val modelFilePath = getModelFilePath(modelBytes, "model_lite.ptl", context)

        return modelFilePath
    }
}

suspend fun getModelFilePath(modelBytes: ByteArray, filename: String, context: Context): String =
    withContext(
        Dispatchers.IO
    ) {
        val file = File(
            context.applicationContext.cacheDir,
            filename
        ) // Use cacheDir for temporary files
        file.writeBytes(modelBytes)
        file.absolutePath
    }
