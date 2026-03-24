package org.contextguard.lib.MLKit.messageClassification

import android.content.Context
import dev.kursor.ktensorflow.api.ModelDesc
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.contextguard.lib.library.generated.resources.Res
import java.io.File

actual class LoadModel(private val context: Context) {
    actual suspend fun getTextModelDesc(): ModelDesc {
        cached?.let { return it }
        return mutex.withLock {
            cached ?: run {
                // Clean up any leftover temp files from previous runs.
                context.cacheDir.listFiles { f -> f.name.startsWith("msg_model") && f.name.endsWith(".tflite") }
                    ?.forEach { it.delete() }

                val bytes = Res.readBytes("files/messageClassificationModel/otis_spam_model.tflite")
                val tmpFile = File.createTempFile("msg_model", ".tflite", context.cacheDir)
                tmpFile.writeBytes(bytes)
                ModelDesc.File(tmpFile).also { cached = it }
            }
        }
    }

    companion object {
        private val mutex = Mutex()
        @Volatile private var cached: ModelDesc? = null
    }
}