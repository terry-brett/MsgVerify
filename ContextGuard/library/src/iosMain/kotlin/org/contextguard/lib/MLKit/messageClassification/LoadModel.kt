package org.contextguard.lib.MLKit.messageClassification

import dev.kursor.ktensorflow.api.ModelDesc
import org.contextguard.lib.library.generated.resources.Res

actual class LoadModel {
    actual suspend fun getTextModelDesc(): ModelDesc {
        return ModelDesc.PathInBundle(
            Res.getUri("files/messageClassificationModel/otis_spam_model.tflite")
                .removePrefix("file://")
        )
    }
}