package org.contextguard.models

/**
 * The result represents a score given to analysed message
 *
 * @param score: **Nullable** Score out of 100 which specifies confidence of message being safe
 * @param textClassificationResult: Result of classifying text as safe or unsafe, if the text is
 *   unsafe, the result will contain a list of reasons
 * @param extractedUrls: List of URLs extracted from the content, corresponding to urlScores
 */
data class Result(
    val urlScores: List<Float>?,
    val textClassificationResult: TextClassificationResult,
    val extractedUrls: List<String>? = null,
)

sealed interface TextClassificationResult {
  data object Safe : TextClassificationResult

  data class Unsafe(val listOfReasons: List<Reason>) : TextClassificationResult
}
