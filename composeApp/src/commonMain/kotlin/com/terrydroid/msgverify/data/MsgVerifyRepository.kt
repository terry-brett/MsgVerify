package com.terrydroid.msgverify.data

import com.terrydroid.msgverify.PlatformContext
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

  fun getContentVerifier(): ContentVerifier = verifier

  suspend fun verifyContent(
      input: String,
      sender: String,
  ): Flow<Result<ContentVerificationResponse>> {
      val result = withContext(Dispatchers.IO) {
          verifier.verify(content = input, sender = sender)
      }

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
        flowOf(
            Result.success(
                ContentVerificationResponse.Safe(
                    result.urlScores,
                    result.extractedUrls
                )
            )
        )
      }
    }
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
