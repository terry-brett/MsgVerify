package org.contextguard.lib.MLKit

interface IUrlPrediction {
    suspend fun makePrediction(url: String): String
}
