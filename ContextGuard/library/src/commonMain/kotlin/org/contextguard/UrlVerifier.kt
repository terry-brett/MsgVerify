package org.contextguard

expect class UrlVerifier {
    suspend fun makePrediction(modelPath: String)
}