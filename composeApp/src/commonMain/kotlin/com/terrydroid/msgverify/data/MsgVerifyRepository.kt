package com.terrydroid.msgverify.data

import com.terrydroid.msgverify.PlatformContext
import com.terrydroid.msgverify.config.MsgVerifyConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import org.contextguard.lib.ContentVerifier
import org.contextguard.lib.ContentVerifierImpl
import org.contextguard.models.Reason
import org.contextguard.models.TextClassificationResult
import org.koin.core.component.KoinComponent

class MsgVerifyRepository(val platformContext: PlatformContext) : KoinComponent {

  private val verifier by lazy { ContentVerifierImpl(platformContext.getNativeContext()) }
  private var logSequence = 0L

  fun getContentVerifier(): ContentVerifier = verifier

  suspend fun verifyContent(
      input: String,
      sender: String,
  ): Flow<Result<ContentVerificationResponse>> {
      val result = withContext(Dispatchers.IO) {
          verifier.verify(content = input, sender = sender)
      }

    if (result == null) {
      logEvent("VERIFICATION_FAILED", input, null, null)
      return flowOf(Result.failure(Exception("Unable to verify content")))
    }

    return when (val classificationResult = result.textClassificationResult) {
      is TextClassificationResult.Unsafe -> {
        val response = ContentVerificationResponse.Unsafe(
            result.urlScores,
            classificationResult.listOfReasons,
            result.extractedUrls,
        )
        logEvent("UNSAFE", input, result.urlScores, classificationResult.listOfReasons)
        flowOf(Result.success(response))
      }
      is TextClassificationResult.Safe -> {
        val response = ContentVerificationResponse.Safe(
            result.urlScores,
            result.extractedUrls
        )
        logEvent("SAFE", input, result.urlScores, null)
        flowOf(Result.success(response))
      }
    }
  }

  /**
   * Logging hook. When MsgVerifyConfig.enableLogging is true,
   * logs verification events for data collection.
   *
   * This method can be customised to::
   * - Write to a local database
   * - Export to a data file
   * - Send to a secure backend (with participant consent)
   */
  private fun logEvent(
      classification: String,
      input: String,
      urlScores: List<Float>?,
      reasons: List<Reason>?
  ) {
    if (!MsgVerifyConfig.enableLogging) return

    logSequence++
    val maxUrlScore = urlScores?.maxOrNull() ?: 0f
    val reasonLabels = reasons?.joinToString(";") { it.reason } ?: ""

    // Uncomment to customise this output format for your data collection needs
    // println("[LOG] seq=$logSequence | $classification | urlScore=$maxUrlScore | reasons=$reasonLabels | inputLength=${input.length}")
  }
}

sealed class ContentVerificationResponse {
  data class Safe(
      val urlScores: List<Float>?,
      val extractedUrls: List<String>? = null
  ) : ContentVerificationResponse()

  data class Unsafe(
      val urlScores: List<Float>?,
      val reasons: List<Reason>,
      val extractedUrls: List<String>? = null
  ) : ContentVerificationResponse()
}
