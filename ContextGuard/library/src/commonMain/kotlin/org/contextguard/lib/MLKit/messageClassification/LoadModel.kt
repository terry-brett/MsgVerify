package org.contextguard.lib.MLKit.messageClassification

import dev.kursor.ktensorflow.api.ModelDesc

expect class LoadModel {
    suspend fun getTextModelDesc(): ModelDesc
}
