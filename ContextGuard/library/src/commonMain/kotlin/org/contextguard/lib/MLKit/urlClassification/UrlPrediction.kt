package org.contextguard.lib.MLKit.urlClassification

/**
 * Expect declaration for the URL prediction class.
 * The constructor takes a platform-specific context, which is then passed
 * to the LoadModel to facilitate model loading.
 */
expect class UrlPrediction(platformContext: Any) {
    /**
     * Suspended function to make a prediction for a given URL.
     * Internally uses LoadModel to load the necessary ML model.
     * @param url The URL string to make a prediction for.
     * @return A score (0–100) representing the probability of the URL being malicious.
     */
    suspend fun makePrediction(url: String): Float
}
