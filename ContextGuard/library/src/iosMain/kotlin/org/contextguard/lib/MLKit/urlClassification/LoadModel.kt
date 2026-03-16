package org.contextguard.lib.MLKit.urlClassification

import dev.kursor.ktensorflow.api.ModelDesc
import org.contextguard.lib.library.generated.resources.Res

actual class LoadModel {
    actual suspend fun getUrlModelDesc(): ModelDesc {
        return ModelDesc.PathInBundle(
            Res.getUri("files/urlClassificationModel/model_lite.tflite")
                .removePrefix("file://")
        )
    }
}