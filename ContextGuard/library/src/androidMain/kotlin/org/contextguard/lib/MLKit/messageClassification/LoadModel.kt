package org.contextguard.lib.MLKit.messageClassification

import android.content.Context
import dev.kursor.ktensorflow.api.ModelDesc
import org.contextguard.lib.library.generated.resources.Res
import java.io.File

actual class LoadModel(private val context: Context) {
    actual suspend fun getTextModelDesc(): ModelDesc {
        val bytes = Res.readBytes("files/messageClassificationModel/otis_spam_model.tflite")
        val tmpFile = File.createTempFile("msg_model", ".tflite", context.cacheDir)
        tmpFile.deleteOnExit()
        tmpFile.writeBytes(bytes)
        return ModelDesc.File(tmpFile)
    }
}