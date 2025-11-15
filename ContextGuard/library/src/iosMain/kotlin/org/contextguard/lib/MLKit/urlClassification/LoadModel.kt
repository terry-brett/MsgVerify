package org.contextguard.lib.MLKit.urlClassification

import dev.kursor.ktensorflow.api.ModelDesc
import org.contextguard.lib.library.generated.resources.Res

actual class LoadModel {
    actual suspend fun getUrlModelDesc(): ModelDesc {
        val modelDesc = ModelDesc.PathInBundle(
            Res.getUri("files/model_lite.tflite")
                .removePrefix("file://")
        )
        return modelDesc
    }
}