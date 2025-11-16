package org.contextguard.lib.MLKit.messageClassification

import dev.kursor.ktensorflow.api.ModelDesc
import org.contextguard.lib.library.generated.resources.Res

actual class LoadModel {
    actual suspend fun getTextModelDesc(): ModelDesc {
        val modelDesc = ModelDesc.PathInBundle(
            Res.getUri("files/messageClassificationModel/distilbert_mnli_dynamic_quant.tflite")
                .removePrefix("file://")
        )
        return modelDesc
    }
}