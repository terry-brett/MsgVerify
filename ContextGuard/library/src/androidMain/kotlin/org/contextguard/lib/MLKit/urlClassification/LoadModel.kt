package org.contextguard.lib.MLKit.urlClassification

import android.content.Context
import dev.kursor.ktensorflow.api.ModelDesc
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.contextguard.lib.library.generated.resources.Res
import java.io.File

actual class LoadModel(private val context: Context) {
    actual suspend fun getUrlModelDesc(): ModelDesc {
        cached?.let { return it }
        return mutex.withLock {
            cached ?: run {
                val bytes = Res.readBytes("files/urlClassificationModel/model_lite.tflite")
                // Use a fixed filename to avoid accumulating temp files
                val modelFile = File(context.cacheDir, "url_model.tflite")
                modelFile.writeBytes(bytes)
                ModelDesc.File(modelFile).also { cached = it }
            }
        }
    }

    companion object {
        private val mutex = Mutex()
        @Volatile private var cached: ModelDesc? = null
    }
}