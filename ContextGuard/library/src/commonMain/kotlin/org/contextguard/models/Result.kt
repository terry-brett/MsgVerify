package org.contextguard.models

/**
 * The result represents a score given to analysed message
 *
 * @param urlScores: **Nullable** List of scores (0-100) for each extracted URL indicating maliciousness
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
