package org.contextguard.lib.MLKit.urlClassification

interface IUrlPrediction {
    suspend fun makePrediction(url: String): String
}
