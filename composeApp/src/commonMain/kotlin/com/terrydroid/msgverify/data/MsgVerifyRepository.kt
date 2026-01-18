package com.terrydroid.msgverify.data

import com.terrydroid.msgverify.PlatformContext
import kotlin.collections.firstOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.contextguard.lib.ContentVerifierImpl
import org.contextguard.models.Reason
import org.contextguard.models.TextClassificationResult
import org.koin.core.component.KoinComponent

class MsgVerifyRepository(val platformContext: PlatformContext) : KoinComponent {

  suspend fun verifyLink(url: String): Flow<Result<LinkVerificationResponse>> {
    val result =
        ContentVerifierImpl(platformContext.getNativeContext())
            .verifyUrl(url)
            ?.urlScores
            ?.firstOrNull() ?: 0f

    return flowOf(
        Result.success(LinkVerificationResponse(maliciousScore = result, description = ""))
    )
  }

  suspend fun verifyContent(
      input: String,
      sender: String,
  ): Flow<Result<ContentVerificationResponse>> {
    val result =
        ContentVerifierImpl(platformContext.getNativeContext())
            .verify(content = input, sender = sender)

    if (result == null) {
      return flowOf(Result.failure(Exception("Unable to verify content")))
    }

    return when (val classificationResult = result.textClassificationResult) {
      is TextClassificationResult.Unsafe -> {
        flowOf(
            Result.success(
                ContentVerificationResponse.Unsafe(
                    result.urlScores,
                    classificationResult.listOfReasons,
                    result.extractedUrls,
                )
            )
        )
      }
      is TextClassificationResult.Safe -> {
        flowOf(Result.success(ContentVerificationResponse.Safe))
      }
    }
  }
}

/**
 * After asking the machine learning model if the link is malicious, it will return a score. This is
 * the @property maliciousScore. This is a percentage 0 - 1
 *
 * @property description - Description of why the model thinks it has this score
 */
data class LinkVerificationResponse(val maliciousScore: Float, val description: String)

sealed class ContentVerificationResponse {
  data object Safe : ContentVerificationResponse()

  data class Unsafe(
      val urlScores: List<Float>?,
      val reasons: List<Reason>,
      val extractedUrls: List<String>? = null
  ) : ContentVerificationResponse()
}
