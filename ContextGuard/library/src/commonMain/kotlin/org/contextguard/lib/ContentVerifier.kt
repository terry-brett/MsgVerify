package org.contextguard.lib

import MessageReasoningLabels
import org.contextguard.addLinkWarningReason
import org.contextguard.lib.MLKit.messageClassification.MessagePrediction
import org.contextguard.lib.MLKit.urlClassification.UrlPrediction
import org.contextguard.models.Reason
import org.contextguard.models.Result
import org.contextguard.models.TextClassificationResult

/**
 * ContentVerifier is the main entry point for verifying content safety.
 *
 * This interface provides a simple API to analyze message content (text, URLs, or both) and
 * determine if it contains potentially malicious or suspicious elements.
 *
 * ## Features
 * - Detects phishing attempts and social engineering
 * - Identifies malicious URLs using ML-based prediction
 * - Classifies text content for suspicious patterns
 * - Extracts and validates URLs from message content automatically
 *
 * ## Usage Example
 *
 * ```kotlin
 * val verifier = ContentVerifier.create(platformContext)
 *
 * // Verify a message with embedded URLs
 * val result = verifier.verify("Check this out: https://example.com")
 *
 * // Handle the result
 * when (result.textClassificationResult) {
 *     is TextClassificationResult.Safe -> {
 *         // Content is safe
 *     }
 *     is TextClassificationResult.Unsafe -> {
 *         // Content contains suspicious elements
 *         result.textClassificationResult.listOfReasons.forEach { reason ->
 *             println("Alert: ${reason.description}")
 *         }
 *     }
 * }
 * ```
 *
 * ## Result Interpretation The returned [Result] object contains:
 * - `urlScore`: Confidence score (0-100) indicating URL maliciousness if URLs were found, null
 *   otherwise
 * - `textClassificationResult`: Classification of the content as [TextClassificationResult.Safe] or
 *   [TextClassificationResult.Unsafe] with a list of detected reasons if unsafe
 */
interface ContentVerifier {
  /**
   * Verifies the safety of the provided content.
   *
   * Analyzes the entire content string for potentially malicious elements including:
   * - Text patterns indicative of phishing or social engineering
   * - Embedded URLs that are automatically extracted and evaluated
   * - Combined threats from both text and URL analysis
   *
   * @param content The content to verify. Can include plain text, URLs, or a combination of both.
   * @param sender The sender of the content we want to verify.
   * @return A [Result] object containing the verification analysis. If this is null, there was some
   *   trouble with the analysis or the input content was not present.
   */
  suspend fun verify(content: String, sender: String): Result?

  /**
   * Verifies the safety of the provided url.
   *
   * @param url The url you want to analyse
   * @return A [Result] object containing the verification analysis. If this is null, there was some
   *   trouble with the analysis or the input content was not present.
   */
  suspend fun verifyUrl(url: String): Result?
}

class ContentVerifierImpl(private val platformContext: Any) : ContentVerifier {

  override suspend fun verify(
      content: String,
      sender: String,
  ): Result? {

    if (content.isEmpty()) return null
    val urlPredictionScores =
        extractUrlsFromContent(content).map { url ->
          UrlPrediction(platformContext).makePrediction(url)
        }

    val isMessageSpam = MessagePrediction(platformContext).isSpam(content)
    var listOfReasons: List<Reason> = listOf()

    if (isMessageSpam) {
      listOfReasons = MessageReasoningLabels(content, sender.orEmpty()).addLabels()
    }

    urlPredictionScores.forEach { listOfReasons.toMutableList().addLinkWarningReason(it) }

    return if (isMessageSpam) {
      Result(
          urlScores = urlPredictionScores,
          textClassificationResult = TextClassificationResult.Unsafe(listOfReasons),
      )
    } else {
      Result(
          urlScores = urlPredictionScores,
          textClassificationResult = TextClassificationResult.Safe,
      )
    }
  }

  override suspend fun verifyUrl(url: String): Result? {
    if (url.isEmpty()) return null

    val urlPredictionScore = UrlPrediction(platformContext).makePrediction(url)

    return Result(
        urlScores = listOf(urlPredictionScore),
        textClassificationResult = TextClassificationResult.Safe,
    )
  }

  private fun extractUrlsFromContent(content: String): List<String> {
    val urlPattern = "https?://[^\\s/$.?#].[^\\s]*".toRegex()
    return urlPattern.findAll(content).map { it.value }.toList()
  }
}
